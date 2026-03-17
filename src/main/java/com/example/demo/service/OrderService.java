package com.example.demo.service;

import com.example.demo.domain.Order;
import com.example.demo.dto.CursorPageResponse;
import com.example.demo.dto.OrderDto;
import com.example.demo.dto.OrderSearchCondition;
import com.example.demo.repository.OrderJdbcRepository;
import com.example.demo.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * 주문 서비스
 *
 * 대용량 데이터 조회 전략:
 *
 * 1. Cursor(Keyset) Pagination - 웹 UI용, 무한스크롤/더보기 패턴
 *    - WHERE id > cursor ORDER BY id LIMIT size
 *    - offset 기반보다 수십~수백 배 빠름 (인덱스만 타므로)
 *
 * 2. Stream 기반 조회 - 대용량 데이터 일괄 처리/내보내기
 *    - PostgreSQL 서버 사이드 커서 활용
 *    - 전체 결과를 메모리에 올리지 않음
 *
 * 3. Batch 처리 - 대량 insert/update 시
 *    - JDBC batchUpdate 활용
 */
@Service
public class OrderService {

    private static final Logger log = Logger.getLogger(OrderService.class.getName());

    private final OrderRepository orderRepository;
    private final OrderJdbcRepository orderJdbcRepository;

    public OrderService(OrderRepository orderRepository, OrderJdbcRepository orderJdbcRepository) {
        this.orderRepository = orderRepository;
        this.orderJdbcRepository = orderJdbcRepository;
    }

    // ──────────────────────────────────────────────
    // 1) Cursor 기반 페이징 조회 (대용량 최적)
    // ──────────────────────────────────────────────

    /**
     * Cursor(Keyset) 기반으로 주문을 페이징 조회한다.
     *
     * offset 기반 페이징(LIMIT 50 OFFSET 100000)은 100,000행을 스캔 후 버리므로
     * 데이터가 많을수록 느려지지만, cursor 기반은 항상 인덱스 range scan만 수행하여 일정한 성능을 보장한다.
     *
     * @param condition 검색 조건 (cursor, size, customerName, status)
     * @return CursorPageResponse 커서 기반 페이징 응답
     */
    @Transactional(readOnly = true)
    public CursorPageResponse<OrderDto> findOrdersByCursor(OrderSearchCondition condition) {
        Long cursor = condition.getCursor() == null ? 0L : condition.getCursor();
        // 다음 페이지 존재 여부 확인을 위해 size + 1개 조회
        int fetchSize = condition.getSize() + 1;

        // 빈 문자열을 null로 변환 (폼에서 빈 값이 ""로 넘어오는 문제 방지)
        String customerName = (condition.getCustomerName() != null && !condition.getCustomerName().isBlank())
                ? condition.getCustomerName() : null;

        Order.OrderStatus status = null;
        if (condition.getStatus() != null && !condition.getStatus().isBlank()) {
            status = Order.OrderStatus.valueOf(condition.getStatus());
        }

        List<Order> orders;
        if (customerName != null || status != null) {
            orders = orderRepository.findByCursorWithCondition(
                    cursor, fetchSize, customerName, status);
        } else {
            orders = orderRepository.findByCursor(cursor, fetchSize);
        }

        boolean hasNext = orders.size() > condition.getSize();

        List<OrderDto> content = orders.stream()
                .limit(condition.getSize())
                .map(OrderDto::from)
                .toList();

        Long nextCursor = content.isEmpty() ? null : content.getLast().getId();

        return CursorPageResponse.<OrderDto>builder()
                .content(content)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .size(condition.getSize())
                .build();
    }

    // ──────────────────────────────────────────────
    // 2) Stream 기반 조회 (대용량 export/처리용)
    // ──────────────────────────────────────────────

    /**
     * JPA Stream API로 전체 주문을 스트리밍 조회한다.
     * PostgreSQL 서버 사이드 커서를 사용하여 메모리 효율적으로 처리.
     *
     * @return 전체 주문 수
     */
    @Transactional(readOnly = true)
    public long processAllOrdersByStream() {
        AtomicLong count = new AtomicLong(0);

        orderRepository.streamAll().forEach(order -> {
            // 실제 비즈니스 로직: CSV 내보내기, 통계 집계, 외부 API 호출 등
            count.incrementAndGet();
            if (count.get() % 10000 == 0) {
                log.info("Stream 처리 진행 중: " + count.get() + " 건 완료");
            }
        });

        log.info("Stream 처리 완료. 총 " + count.get() + " 건");
        return count.get();
    }

    // ──────────────────────────────────────────────
    // 3) JDBC Batch 기반 스트리밍 (가장 로우 레벨, 가장 효율적)
    // ──────────────────────────────────────────────

    /**
     * JDBC 레벨에서 배치 단위로 주문을 처리한다.
     * batchSize만큼씩 읽어서 콜백 처리 → 메모리 부담 최소.
     *
     * @param batchSize 배치 크기
     * @return 처리된 총 건수
     */
    @Transactional(readOnly = true)
    public long processOrdersByBatch(int batchSize) {
        AtomicLong totalProcessed = new AtomicLong(0);

        orderJdbcRepository.batchProcessOrders(batchSize, batch -> {
            totalProcessed.addAndGet(batch.size());
            log.info("배치 처리: " + batch.size() + " 건 (누적: " + totalProcessed.get() + " 건)");
            // 배치 단위 비즈니스 로직 수행
        });

        log.info("배치 처리 완료. 총 " + totalProcessed.get() + " 건");
        return totalProcessed.get();
    }

    // ──────────────────────────────────────────────
    // 기본 CRUD
    // ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public OrderDto findById(Long id) {
        return orderRepository.findById(id)
                .map(OrderDto::from)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. ID: " + id));
    }

    @Transactional
    public OrderDto save(Order order) {
        Order saved = orderRepository.save(order);
        return OrderDto.from(saved);
    }

    @Transactional(readOnly = true)
    public long count() {
        return orderRepository.count();
    }
}
