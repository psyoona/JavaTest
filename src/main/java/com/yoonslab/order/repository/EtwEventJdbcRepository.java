package com.yoonslab.order.repository;

import com.yoonslab.order.domain.EtwEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

/**
 * ETW 이벤트 JDBC 배치 INSERT 레포지토리.
 *
 * <p>JPA 대신 raw JDBC를 사용하여 벌크 INSERT 성능을 극대화한다.
 * PostgreSQL의 경우 reWriteBatchedInserts=true 옵션과 함께
 * multi-value INSERT로 자동 변환되어 네트워크 라운드트립을 최소화한다.</p>
 */
@Repository
public class EtwEventJdbcRepository {

    private static final Logger log = LoggerFactory.getLogger(EtwEventJdbcRepository.class);

    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL = """
            INSERT INTO REQUEST_LOGS (
                SERVER_ID, EVENT_TIMESTAMP, HTTP_METHOD, URL, STATUS_CODE,
                CLIENT_IP, REQUEST_START_TIME, RESPONSE_END_TIME, DURATION_MS,
                BYTES_SENT, BYTES_RECEIVED, USER_AGENT, REFERER,
                PROTOCOL_VERSION, REQUEST_HEADER, RESPONSE_HEADER
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    public EtwEventJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 이벤트 목록을 JDBC batch INSERT로 저장.
     * @param events 저장할 이벤트 목록
     * @return 저장된 건수
     */
    public int batchInsert(List<EtwEvent> events) {
        if (events.isEmpty()) return 0;

        int[][] result = jdbcTemplate.batchUpdate(INSERT_SQL, events, 500,
                (PreparedStatement ps, EtwEvent e) -> {
                    ps.setInt(1, e.getServerId());
                    ps.setTimestamp(2, Timestamp.valueOf(e.getEventTimestamp()));

                    setNullableString(ps, 3, e.getHttpMethod());
                    setNullableString(ps, 4, e.getUrl());
                    setNullableShort(ps, 5, e.getStatusCode());
                    setNullableString(ps, 6, e.getClientIp());
                    setNullableTimestamp(ps, 7, e.getRequestStartTime());
                    setNullableTimestamp(ps, 8, e.getResponseEndTime());
                    setNullableBigDecimal(ps, 9, e.getDurationMs());
                    setNullableLong(ps, 10, e.getBytesSent());
                    setNullableLong(ps, 11, e.getBytesReceived());
                    setNullableString(ps, 12, e.getUserAgent());
                    setNullableString(ps, 13, e.getReferer());
                    setNullableString(ps, 14, e.getProtocolVersion());
                    setNullableString(ps, 15, e.getRequestHeader());
                    setNullableString(ps, 16, e.getResponseHeader());
                });

        int total = 0;
        for (int[] batch : result) {
            for (int count : batch) {
                if (count > 0) total += count;
                else total++; // Statement.SUCCESS_NO_INFO (-2)
            }
        }

        log.debug("ETW 이벤트 batch INSERT 완료: {}건", total);
        return total;
    }

    // ── Nullable 헬퍼 ──────────────────────────────

    private void setNullableString(PreparedStatement ps, int idx, String value)
            throws java.sql.SQLException {
        if (value != null) ps.setString(idx, value);
        else ps.setNull(idx, Types.VARCHAR);
    }

    private void setNullableShort(PreparedStatement ps, int idx, Short value)
            throws java.sql.SQLException {
        if (value != null) ps.setShort(idx, value);
        else ps.setNull(idx, Types.SMALLINT);
    }

    private void setNullableLong(PreparedStatement ps, int idx, Long value)
            throws java.sql.SQLException {
        if (value != null) ps.setLong(idx, value);
        else ps.setNull(idx, Types.BIGINT);
    }

    private void setNullableTimestamp(PreparedStatement ps, int idx, java.time.LocalDateTime value)
            throws java.sql.SQLException {
        if (value != null) ps.setTimestamp(idx, Timestamp.valueOf(value));
        else ps.setNull(idx, Types.TIMESTAMP);
    }

    private void setNullableBigDecimal(PreparedStatement ps, int idx, java.math.BigDecimal value)
            throws java.sql.SQLException {
        if (value != null) ps.setBigDecimal(idx, value);
        else ps.setNull(idx, Types.DECIMAL);
    }
}
