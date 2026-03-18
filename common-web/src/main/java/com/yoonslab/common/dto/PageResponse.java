package com.yoonslab.common.dto;

import java.util.List;

/**
 * 번호 기반 페이징 응답 DTO (공용 라이브러리)
 * 1~10 페이지 번호 네비게이션 지원
 * 도메인에 독립적 - 어떤 모듈에서도 재사용 가능
 *
 * @param <T> 데이터 타입
 */
public class PageResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private int startPage;
    private int endPage;
    private boolean hasPrevious;
    private boolean hasNext;

    public PageResponse() {
    }

    public PageResponse(List<T> content, int page, int size, long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);

        // 10개 단위 페이지 블록 계산 (1~10, 11~20, ...)
        int blockSize = 10;
        this.startPage = ((page - 1) / blockSize) * blockSize + 1;
        this.endPage = Math.min(startPage + blockSize - 1, totalPages);
        this.hasPrevious = startPage > 1;
        this.hasNext = endPage < totalPages;
    }

    // Getters

    public List<T> getContent() { return content; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public int getStartPage() { return startPage; }
    public int getEndPage() { return endPage; }
    public boolean isHasPrevious() { return hasPrevious; }
    public boolean isHasNext() { return hasNext; }
}
