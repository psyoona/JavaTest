package com.yoonslab.order.dto;

/**
 * 주문 검색 조건 DTO
 */
public class OrderSearchCondition {

    private String customerName;
    private String productName;
    private String status;

    /** Cursor 기반 페이징: 이전 페이지 마지막 ID */
    private Long cursor;

    /** 페이지 크기 (기본 50) */
    private int size = 50;

    public OrderSearchCondition() {
    }

    public OrderSearchCondition(String customerName, String productName, String status, Long cursor, int size) {
        this.customerName = customerName;
        this.productName = productName;
        this.status = status;
        this.cursor = cursor;
        this.size = size;
    }

    // Getters & Setters

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getCursor() { return cursor; }
    public void setCursor(Long cursor) { this.cursor = cursor; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    // Builder

    public static OrderSearchConditionBuilder builder() { return new OrderSearchConditionBuilder(); }

    public static class OrderSearchConditionBuilder {
        private String customerName;
        private String productName;
        private String status;
        private Long cursor;
        private int size = 50;

        public OrderSearchConditionBuilder customerName(String customerName) { this.customerName = customerName; return this; }
        public OrderSearchConditionBuilder productName(String productName) { this.productName = productName; return this; }
        public OrderSearchConditionBuilder status(String status) { this.status = status; return this; }
        public OrderSearchConditionBuilder cursor(Long cursor) { this.cursor = cursor; return this; }
        public OrderSearchConditionBuilder size(int size) { this.size = size; return this; }

        public OrderSearchCondition build() {
            return new OrderSearchCondition(customerName, productName, status, cursor, size);
        }
    }
}
