package com.yoonslab.order.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ETW (Event Tracing for Windows) HTTP 요청 이벤트 도메인 객체.
 * JPA 엔티티가 아닌 순수 POJO — JDBC batch insert 전용.
 */
public class EtwEvent {

    private Long requestId;
    private Integer serverId;
    private LocalDateTime eventTimestamp;
    private String httpMethod;
    private String url;
    private Short statusCode;
    private String clientIp;
    private LocalDateTime requestStartTime;
    private LocalDateTime responseEndTime;
    private BigDecimal durationMs;
    private Long bytesSent;
    private Long bytesReceived;
    private String userAgent;
    private String referer;
    private String protocolVersion;
    private String requestHeader;
    private String responseHeader;
    private LocalDateTime createdAt;

    // ── Builder ──────────────────────────────────

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final EtwEvent instance = new EtwEvent();

        public Builder requestId(Long v)              { instance.requestId = v; return this; }
        public Builder serverId(Integer v)             { instance.serverId = v; return this; }
        public Builder eventTimestamp(LocalDateTime v)  { instance.eventTimestamp = v; return this; }
        public Builder httpMethod(String v)            { instance.httpMethod = v; return this; }
        public Builder url(String v)                   { instance.url = v; return this; }
        public Builder statusCode(Short v)             { instance.statusCode = v; return this; }
        public Builder clientIp(String v)              { instance.clientIp = v; return this; }
        public Builder requestStartTime(LocalDateTime v) { instance.requestStartTime = v; return this; }
        public Builder responseEndTime(LocalDateTime v)  { instance.responseEndTime = v; return this; }
        public Builder durationMs(BigDecimal v)        { instance.durationMs = v; return this; }
        public Builder bytesSent(Long v)               { instance.bytesSent = v; return this; }
        public Builder bytesReceived(Long v)           { instance.bytesReceived = v; return this; }
        public Builder userAgent(String v)             { instance.userAgent = v; return this; }
        public Builder referer(String v)               { instance.referer = v; return this; }
        public Builder protocolVersion(String v)       { instance.protocolVersion = v; return this; }
        public Builder requestHeader(String v)         { instance.requestHeader = v; return this; }
        public Builder responseHeader(String v)        { instance.responseHeader = v; return this; }
        public Builder createdAt(LocalDateTime v)      { instance.createdAt = v; return this; }

        public EtwEvent build() { return instance; }
    }

    // ── Getters ──────────────────────────────────

    public Long getRequestId()                { return requestId; }
    public Integer getServerId()              { return serverId; }
    public LocalDateTime getEventTimestamp()   { return eventTimestamp; }
    public String getHttpMethod()             { return httpMethod; }
    public String getUrl()                    { return url; }
    public Short getStatusCode()              { return statusCode; }
    public String getClientIp()               { return clientIp; }
    public LocalDateTime getRequestStartTime() { return requestStartTime; }
    public LocalDateTime getResponseEndTime()  { return responseEndTime; }
    public BigDecimal getDurationMs()         { return durationMs; }
    public Long getBytesSent()                { return bytesSent; }
    public Long getBytesReceived()            { return bytesReceived; }
    public String getUserAgent()              { return userAgent; }
    public String getReferer()                { return referer; }
    public String getProtocolVersion()        { return protocolVersion; }
    public String getRequestHeader()          { return requestHeader; }
    public String getResponseHeader()         { return responseHeader; }
    public LocalDateTime getCreatedAt()       { return createdAt; }
}
