package com.yoonslab.order.dto;

import com.yoonslab.order.domain.EtwEvent;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ETW 이벤트 수신용 DTO.
 * 클라이언트(ETW 수집기)에서 전송하는 JSON 구조와 1:1 매핑.
 */
public class EtwEventDto {

    @NotNull(message = "serverId는 필수입니다")
    private Integer serverId;

    @NotNull(message = "eventTimestamp는 필수입니다")
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

    /**
     * DTO → 도메인 변환
     */
    public EtwEvent toEntity() {
        return EtwEvent.builder()
                .serverId(serverId)
                .eventTimestamp(eventTimestamp)
                .httpMethod(httpMethod)
                .url(url)
                .statusCode(statusCode)
                .clientIp(clientIp)
                .requestStartTime(requestStartTime)
                .responseEndTime(responseEndTime)
                .durationMs(durationMs)
                .bytesSent(bytesSent)
                .bytesReceived(bytesReceived)
                .userAgent(userAgent)
                .referer(referer)
                .protocolVersion(protocolVersion)
                .requestHeader(requestHeader)
                .responseHeader(responseHeader)
                .build();
    }

    // ── Getters / Setters (Jackson 역직렬화용) ──

    public Integer getServerId()              { return serverId; }
    public void setServerId(Integer v)        { this.serverId = v; }

    public LocalDateTime getEventTimestamp()   { return eventTimestamp; }
    public void setEventTimestamp(LocalDateTime v) { this.eventTimestamp = v; }

    public String getHttpMethod()             { return httpMethod; }
    public void setHttpMethod(String v)       { this.httpMethod = v; }

    public String getUrl()                    { return url; }
    public void setUrl(String v)              { this.url = v; }

    public Short getStatusCode()              { return statusCode; }
    public void setStatusCode(Short v)        { this.statusCode = v; }

    public String getClientIp()               { return clientIp; }
    public void setClientIp(String v)         { this.clientIp = v; }

    public LocalDateTime getRequestStartTime() { return requestStartTime; }
    public void setRequestStartTime(LocalDateTime v) { this.requestStartTime = v; }

    public LocalDateTime getResponseEndTime()  { return responseEndTime; }
    public void setResponseEndTime(LocalDateTime v) { this.responseEndTime = v; }

    public BigDecimal getDurationMs()         { return durationMs; }
    public void setDurationMs(BigDecimal v)   { this.durationMs = v; }

    public Long getBytesSent()                { return bytesSent; }
    public void setBytesSent(Long v)          { this.bytesSent = v; }

    public Long getBytesReceived()            { return bytesReceived; }
    public void setBytesReceived(Long v)      { this.bytesReceived = v; }

    public String getUserAgent()              { return userAgent; }
    public void setUserAgent(String v)        { this.userAgent = v; }

    public String getReferer()                { return referer; }
    public void setReferer(String v)          { this.referer = v; }

    public String getProtocolVersion()        { return protocolVersion; }
    public void setProtocolVersion(String v)  { this.protocolVersion = v; }

    public String getRequestHeader()          { return requestHeader; }
    public void setRequestHeader(String v)    { this.requestHeader = v; }

    public String getResponseHeader()         { return responseHeader; }
    public void setResponseHeader(String v)   { this.responseHeader = v; }
}
