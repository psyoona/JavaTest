package com.example.demo.controller;

import com.example.demo.dto.CursorPageResponse;
import com.example.demo.dto.OrderDto;
import com.example.demo.dto.OrderSearchCondition;
import com.example.demo.dto.PageResponse;
import com.example.demo.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 주문 REST API 컨트롤러
 * JSON 기반의 API 엔드포인트 제공
 */
@RestController
@RequestMapping("/api/orders")
public class OrderApiController {

    private final OrderService orderService;

    public OrderApiController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 번호 기반 페이징 API (1~10 페이지 네비게이션)
     *
     * 사용 예시:
     * - 첫 페이지:   GET /api/orders?page=1&size=50
     * - 3페이지:    GET /api/orders?page=3&size=50
     * - 검색+페이징:  GET /api/orders?page=1&size=50&customerName=홍길동
     */
    @GetMapping
    public ResponseEntity<PageResponse<OrderDto>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String status) {

        return ResponseEntity.ok(
                orderService.findOrdersByPage(page, size, customerName, status));
    }

    /**
     * 주문 상세 API
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> detail(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    /**
     * Stream 기반 전체 처리 실행 API
     * 대용량 데이터를 서버 사이드 커서로 스트리밍하며 처리
     */
    @PostMapping("/process/stream")
    public ResponseEntity<Map<String, Object>> processStream() {
        long startTime = System.currentTimeMillis();
        long count = orderService.processAllOrdersByStream();
        long elapsed = System.currentTimeMillis() - startTime;

        return ResponseEntity.ok(Map.of(
                "method", "JPA Stream (Server-side Cursor)",
                "processedCount", count,
                "elapsedMs", elapsed
        ));
    }

    /**
     * Batch 기반 전체 처리 실행 API
     * JDBC 레벨에서 배치 단위로 읽어서 처리
     */
    @PostMapping("/process/batch")
    public ResponseEntity<Map<String, Object>> processBatch(
            @RequestParam(defaultValue = "1000") int batchSize) {
        // 범위 제한 (최소 100, 최대 10000)
        if (batchSize < 100 || batchSize > 10000) batchSize = 1000;
        long startTime = System.currentTimeMillis();
        long count = orderService.processOrdersByBatch(batchSize);
        long elapsed = System.currentTimeMillis() - startTime;

        return ResponseEntity.ok(Map.of(
                "method", "JDBC Batch Processing",
                "batchSize", batchSize,
                "processedCount", count,
                "elapsedMs", elapsed
        ));
    }

    /**
     * 데이터 건수 조회 API
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> count() {
        return ResponseEntity.ok(Map.of("count", orderService.count()));
    }
}
