package com.yoonslab.order.controller;

import com.yoonslab.common.dto.ApiResponse;
import com.yoonslab.common.dto.PageResponse;
import com.yoonslab.order.dto.OrderDto;
import com.yoonslab.order.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 주문 REST API 컨트롤러
 * 모든 응답은 ApiResponse<T>로 통일 ({ success, message, data })
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
    public ApiResponse<PageResponse<OrderDto>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String status) {

        return ApiResponse.ok(orderService.findOrdersByPage(page, size, customerName, status));
    }

    /**
     * 주문 상세 API
     */
    @GetMapping("/{id}")
    public ApiResponse<OrderDto> detail(@PathVariable Long id) {
        return ApiResponse.ok(orderService.findById(id));
    }

    /**
     * Stream 기반 전체 처리 실행 API
     */
    @PostMapping("/process/stream")
    public ApiResponse<Map<String, Object>> processStream() {
        long startTime = System.currentTimeMillis();
        long count = orderService.processAllOrdersByStream();
        long elapsed = System.currentTimeMillis() - startTime;

        return ApiResponse.ok(Map.of(
                "method", "JPA Stream (Server-side Cursor)",
                "processedCount", count,
                "elapsedMs", elapsed
        ));
    }

    /**
     * Batch 기반 전체 처리 실행 API
     */
    @PostMapping("/process/batch")
    public ApiResponse<Map<String, Object>> processBatch(
            @RequestParam(defaultValue = "1000") int batchSize) {
        // 범위 제한 (최소 100, 최대 10000)
        if (batchSize < 100 || batchSize > 10000) batchSize = 1000;

        long startTime = System.currentTimeMillis();
        long count = orderService.processOrdersByBatch(batchSize);
        long elapsed = System.currentTimeMillis() - startTime;

        return ApiResponse.ok(Map.of(
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
    public ApiResponse<Map<String, Long>> count() {
        return ApiResponse.ok(Map.of("count", orderService.count()));
    }
}
