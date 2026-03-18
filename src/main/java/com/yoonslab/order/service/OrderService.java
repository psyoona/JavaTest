package com.yoonslab.order.service;

import com.yoonslab.common.dto.CursorPageResponse;
import com.yoonslab.common.dto.PageResponse;
import com.yoonslab.order.domain.Order;
import com.yoonslab.order.dto.OrderDto;
import com.yoonslab.order.dto.OrderSearchCondition;
import com.yoonslab.order.repository.OrderJdbcRepository;
import com.yoonslab.order.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * 주문 서비스
 *
 * 페이징 DTO(PageResponse, CursorPageResponse)는 common-web 모듈에서 가져옴
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

    @Transactional(readOnly = true)
    public CursorPageResponse<OrderDto> findOrdersByCursor(OrderSearchCondition condition) {
        Long cursor = condition.getCursor() == null ? 0L : condition.getCursor();
        int fetchSize = condition.getSize() + 1;

        String customerName = (condition.getCustomerName() != null && !condition.getCustomerName().isBlank())
                ? condition.getCustomerName() : null;

        Order.OrderStatus status = null;
        if (condition.getStatus() != null && !condition.getStatus().isBlank()) {
            status = Order.OrderStatus.valueOf(condition.getStatus());
        }

        List<Order> orders;
        if (customerName != null || status != null) {
            orders = orderRepository.findByCursorWithCondition(cursor, fetchSize, customerName, status);
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

    @Transactional(readOnly = true)
    public long processAllOrdersByStream() {
        AtomicLong count = new AtomicLong(0);

        orderRepository.streamAll().forEach(order -> {
            count.incrementAndGet();
            if (count.get() % 10000 == 0) {
                log.info("Stream 처리 진행 중: " + count.get() + " 건 완료");
            }
        });

        log.info("Stream 처리 완료. 총 " + count.get() + " 건");
        return count.get();
    }

    // ──────────────────────────────────────────────
    // 3) JDBC Batch 기반 스트리밍
    // ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public long processOrdersByBatch(int batchSize) {
        AtomicLong totalProcessed = new AtomicLong(0);

        orderJdbcRepository.batchProcessOrders(batchSize, batch -> {
            totalProcessed.addAndGet(batch.size());
            log.info("배치 처리: " + batch.size() + " 건 (누적: " + totalProcessed.get() + " 건)");
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
                .orElseThrow(() -> new NoSuchElementException("주문을 찾을 수 없습니다. ID: " + id));
    }

    @Transactional
    public OrderDto save(Order order) {
        return OrderDto.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public long count() {
        return orderRepository.count();
    }

    // ──────────────────────────────────────────────
    // 4) 번호 기반 페이징 (1~10 페이지 네비게이션)
    // ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<OrderDto> findOrdersByPage(int page, int size,
                                                    String customerName, String statusStr) {
        if (page < 1) page = 1;
        if (size < 1 || size > 200) size = 50;

        String custName = (customerName != null && !customerName.isBlank()) ? customerName : null;

        Order.OrderStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = Order.OrderStatus.valueOf(statusStr);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("유효하지 않은 주문 상태입니다: " + statusStr);
            }
        }

        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by("id").ascending());
        Page<Order> result = orderRepository.findPageWithCondition(custName, status, pageable);

        List<OrderDto> content = result.getContent().stream()
                .map(OrderDto::from)
                .toList();

        return new PageResponse<>(content, page, size, result.getTotalElements());
    }
}
