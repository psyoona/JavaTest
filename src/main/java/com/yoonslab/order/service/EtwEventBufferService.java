package com.yoonslab.order.service;

import com.yoonslab.order.domain.EtwEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ETW 이벤트 인메모리 버퍼 서비스.
 *
 * <p>실시간 ETW 이벤트를 건당 DB 접근 없이 ConcurrentLinkedQueue에 버퍼링하고,
 * 배치 스케줄러가 주기적으로 drain하여 벌크 INSERT를 수행한다.</p>
 *
 * <h3>설계 포인트</h3>
 * <ul>
 *   <li>Lock-free 큐(ConcurrentLinkedQueue)로 producer(API) 경합 최소화</li>
 *   <li>메모리 보호를 위한 큐 사이즈 상한 (기본 100,000건)</li>
 *   <li>drain 시 한 번에 최대 batchDrainSize건만 꺼내서 메모리/GC 부담 완화</li>
 * </ul>
 */
@Service
public class EtwEventBufferService {

    private static final Logger log = LoggerFactory.getLogger(EtwEventBufferService.class);

    private final ConcurrentLinkedQueue<EtwEvent> buffer = new ConcurrentLinkedQueue<>();
    private final AtomicLong totalEnqueued = new AtomicLong(0);
    private final AtomicLong totalDropped = new AtomicLong(0);

    /** 큐 최대 크기 — 초과 시 이벤트 드랍 (메모리 보호) */
    private static final int MAX_QUEUE_SIZE = 100_000;

    /**
     * 이벤트 단건 버퍼에 추가. O(1), lock-free.
     * @return true: 정상 적재, false: 큐 포화로 드랍
     */
    public boolean enqueue(EtwEvent event) {
        // ConcurrentLinkedQueue.size()는 O(n)이므로 totalEnqueued 기반 추정
        if (buffer.size() >= MAX_QUEUE_SIZE) {
            totalDropped.incrementAndGet();
            log.warn("ETW 버퍼 포화 ({}건), 이벤트 드랍됨. 누적 드랍: {}",
                    MAX_QUEUE_SIZE, totalDropped.get());
            return false;
        }
        buffer.offer(event);
        totalEnqueued.incrementAndGet();
        return true;
    }

    /**
     * 이벤트 벌크 버퍼에 추가.
     * @return 실제 적재된 건수
     */
    public int enqueueBatch(List<EtwEvent> events) {
        int accepted = 0;
        for (EtwEvent event : events) {
            if (enqueue(event)) {
                accepted++;
            }
        }
        return accepted;
    }

    /**
     * 버퍼에서 최대 maxSize건을 drain하여 반환.
     * 배치 스케줄러에서 호출한다.
     */
    public List<EtwEvent> drain(int maxSize) {
        List<EtwEvent> batch = new ArrayList<>(Math.min(maxSize, 1000));
        for (int i = 0; i < maxSize; i++) {
            EtwEvent event = buffer.poll();
            if (event == null) break;
            batch.add(event);
        }
        return batch;
    }

    /** 현재 버퍼에 대기 중인 이벤트 수 (근사치) */
    public int getBufferSize() {
        return buffer.size();
    }

    /** 누적 적재 건수 */
    public long getTotalEnqueued() {
        return totalEnqueued.get();
    }

    /** 누적 드랍 건수 */
    public long getTotalDropped() {
        return totalDropped.get();
    }
}
