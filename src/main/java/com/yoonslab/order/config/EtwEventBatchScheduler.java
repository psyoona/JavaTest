package com.yoonslab.order.config;

import com.yoonslab.order.domain.EtwEvent;
import com.yoonslab.order.repository.EtwEventJdbcRepository;
import com.yoonslab.order.service.EtwEventBufferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ETW 이벤트 배치 플러시 스케줄러.
 *
 * <p>주기적으로 인메모리 버퍼를 drain하여 JDBC batch INSERT로 DB에 저장한다.
 * Spring @Scheduled 기반으로 동작하며, 별도 배치 프로그램 없이
 * 애플리케이션 내에서 Consumer 역할을 수행한다.</p>
 *
 * <h3>동작 방식</h3>
 * <ol>
 *   <li>fixedDelay 간격으로 실행 (이전 실행 완료 후 대기)</li>
 *   <li>버퍼에서 최대 drainSize건을 drain</li>
 *   <li>JDBC batch INSERT로 DB 저장</li>
 *   <li>실패 시 로그 기록 (이벤트 유실 — 실시간 모니터링 데이터 특성상 재시도 불필요)</li>
 * </ol>
 */
@Component
public class EtwEventBatchScheduler {

    private static final Logger log = LoggerFactory.getLogger(EtwEventBatchScheduler.class);

    private final EtwEventBufferService bufferService;
    private final EtwEventJdbcRepository repository;

    @Value("${etw.batch.drain-size:5000}")
    private int drainSize;

    public EtwEventBatchScheduler(EtwEventBufferService bufferService,
                                  EtwEventJdbcRepository repository) {
        this.bufferService = bufferService;
        this.repository = repository;
    }

    /**
     * 주기적 배치 플러시.
     * 기본 5초 간격, application.yml의 etw.batch.flush-interval-ms로 조정 가능.
     */
    @Scheduled(fixedDelayString = "${etw.batch.flush-interval-ms:5000}")
    public void flushBuffer() {
        List<EtwEvent> events = bufferService.drain(drainSize);
        if (events.isEmpty()) return;

        try {
            int saved = repository.batchInsert(events);
            log.info("ETW 배치 플러시 완료: {}건 저장 | 버퍼 잔량: {} | 누적: {}",
                    saved, bufferService.getBufferSize(), bufferService.getTotalEnqueued());
        } catch (Exception e) {
            log.error("ETW 배치 플러시 실패: {}건 유실 | 원인: {}",
                    events.size(), e.getMessage(), e);
            // 실시간 모니터링 데이터 → 재시도 대신 유실 허용 (로그로 추적)
        }
    }
}
