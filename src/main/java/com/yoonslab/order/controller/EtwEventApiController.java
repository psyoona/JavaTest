package com.yoonslab.order.controller;

import com.yoonslab.order.dto.EtwEventDto;
import com.yoonslab.order.service.EtwEventBufferService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ETW 이벤트 수집 API 컨트롤러.
 *
 * <p>실시간으로 수신된 ETW 이벤트를 인메모리 버퍼에 적재한다.
 * DB 접근은 하지 않으므로 응답이 매우 빠르다.</p>
 */
@RestController
@RequestMapping("/api/etw")
@Validated
public class EtwEventApiController {

    private final EtwEventBufferService bufferService;

    public EtwEventApiController(EtwEventBufferService bufferService) {
        this.bufferService = bufferService;
    }

    /**
     * 단건 이벤트 수신
     * POST /api/etw/events
     */
    @PostMapping("/events")
    public ResponseEntity<Map<String, Object>> receiveEvent(@Valid @RequestBody EtwEventDto dto) {
        boolean accepted = bufferService.enqueue(dto.toEntity());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("accepted", accepted);
        body.put("bufferSize", bufferService.getBufferSize());

        return accepted
                ? ResponseEntity.accepted().body(body)
                : ResponseEntity.status(503).body(body);
    }

    /**
     * 벌크 이벤트 수신 (배열)
     * POST /api/etw/events/bulk
     */
    @PostMapping("/events/bulk")
    public ResponseEntity<Map<String, Object>> receiveEventsBulk(
            @RequestBody @NotEmpty(message = "이벤트 목록이 비어있습니다")
            List<@Valid EtwEventDto> dtos) {

        List<com.yoonslab.order.domain.EtwEvent> events = dtos.stream()
                .map(EtwEventDto::toEntity)
                .toList();
        int accepted = bufferService.enqueueBatch(events);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("total", dtos.size());
        body.put("accepted", accepted);
        body.put("dropped", dtos.size() - accepted);
        body.put("bufferSize", bufferService.getBufferSize());

        return ResponseEntity.accepted().body(body);
    }

    /**
     * 버퍼 상태 모니터링
     * GET /api/etw/status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("bufferSize", bufferService.getBufferSize());
        body.put("totalEnqueued", bufferService.getTotalEnqueued());
        body.put("totalDropped", bufferService.getTotalDropped());
        return ResponseEntity.ok(body);
    }
}
