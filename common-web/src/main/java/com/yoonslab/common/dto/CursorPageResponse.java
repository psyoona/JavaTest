package com.yoonslab.common.dto;

import java.util.List;

/**
 * Cursor(Keyset) 기반 페이징 응답 DTO (공용 라이브러리)
 * offset 기반 페이징보다 대용량 데이터에서 훨씬 효율적
 * 도메인에 독립적 - 어떤 모듈에서도 재사용 가능
 *
 * @param <T> 데이터 타입
 */
public class CursorPageResponse<T> {

    /** 현재 페이지의 데이터 목록 */
    private List<T> content;

    /** 다음 페이지 조회를 위한 커서 (마지막 요소의 ID) */
    private Long nextCursor;

    /** 다음 페이지 존재 여부 */
    private boolean hasNext;

    /** 요청한 페이지 크기 */
    private int size;

    /** 전체 데이터 수 (선택적 - 대용량 시 count 쿼리 부담 있음) */
    private Long totalElements;

    public CursorPageResponse() {
    }

    public CursorPageResponse(List<T> content, Long nextCursor, boolean hasNext, int size, Long totalElements) {
        this.content = content;
        this.nextCursor = nextCursor;
        this.hasNext = hasNext;
        this.size = size;
        this.totalElements = totalElements;
    }

    // Getters & Setters

    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }

    public Long getNextCursor() { return nextCursor; }
    public void setNextCursor(Long nextCursor) { this.nextCursor = nextCursor; }

    public boolean isHasNext() { return hasNext; }
    public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public Long getTotalElements() { return totalElements; }
    public void setTotalElements(Long totalElements) { this.totalElements = totalElements; }

    // Builder

    public static <T> CursorPageResponseBuilder<T> builder() { return new CursorPageResponseBuilder<>(); }

    public static class CursorPageResponseBuilder<T> {
        private List<T> content;
        private Long nextCursor;
        private boolean hasNext;
        private int size;
        private Long totalElements;

        public CursorPageResponseBuilder<T> content(List<T> content) { this.content = content; return this; }
        public CursorPageResponseBuilder<T> nextCursor(Long nextCursor) { this.nextCursor = nextCursor; return this; }
        public CursorPageResponseBuilder<T> hasNext(boolean hasNext) { this.hasNext = hasNext; return this; }
        public CursorPageResponseBuilder<T> size(int size) { this.size = size; return this; }
        public CursorPageResponseBuilder<T> totalElements(Long totalElements) { this.totalElements = totalElements; return this; }

        public CursorPageResponse<T> build() {
            return new CursorPageResponse<>(content, nextCursor, hasNext, size, totalElements);
        }
    }
}
