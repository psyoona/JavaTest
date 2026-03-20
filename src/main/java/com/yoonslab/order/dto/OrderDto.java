package com.yoonslab.order.dto;

import com.yoonslab.order.domain.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 응답 DTO
 */
public class OrderDto {

	private Long id;
	private String customerName;
	private String productName;
	private Integer quantity;
	private BigDecimal price;
	private BigDecimal totalAmount;
	private String status;
	private LocalDateTime createdAt;

	public OrderDto() {
	}

	public OrderDto(Long id, String customerName, String productName, Integer quantity,
					BigDecimal price, BigDecimal totalAmount, String status, LocalDateTime createdAt) {
		this.id = id;
		this.customerName = customerName;
		this.productName = productName;
		this.quantity = quantity;
		this.price = price;
		this.totalAmount = totalAmount;
		this.status = status;
		this.createdAt = createdAt;
	}

	public static OrderDto from(Order order) {
		return OrderDto.builder()
				.id(order.getId())
				.customerName(order.getCustomerName())
				.productName(order.getProductName())
				.quantity(order.getQuantity())
				.price(order.getPrice())
				.totalAmount(order.getTotalAmount())
				.status(order.getStatus().name())
				.createdAt(order.getCreatedAt())
				.build();
	}

	// Getters & Setters

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getCustomerName() { return customerName; }
	public void setCustomerName(String customerName) { this.customerName = customerName; }

	public String getProductName() { return productName; }
	public void setProductName(String productName) { this.productName = productName; }

	public Integer getQuantity() { return quantity; }
	public void setQuantity(Integer quantity) { this.quantity = quantity; }

	public BigDecimal getPrice() { return price; }
	public void setPrice(BigDecimal price) { this.price = price; }

	public BigDecimal getTotalAmount() { return totalAmount; }
	public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }

	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

	// Builder

	public static OrderDtoBuilder builder() { return new OrderDtoBuilder(); }

	public static class OrderDtoBuilder {
		private Long id;
		private String customerName;
		private String productName;
		private Integer quantity;
		private BigDecimal price;
		private BigDecimal totalAmount;
		private String status;
		private LocalDateTime createdAt;

		public OrderDtoBuilder id(Long id) { this.id = id; return this; }
		public OrderDtoBuilder customerName(String customerName) { this.customerName = customerName; return this; }
		public OrderDtoBuilder productName(String productName) { this.productName = productName; return this; }
		public OrderDtoBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }
		public OrderDtoBuilder price(BigDecimal price) { this.price = price; return this; }
		public OrderDtoBuilder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
		public OrderDtoBuilder status(String status) { this.status = status; return this; }
		public OrderDtoBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

		public OrderDto build() {
			return new OrderDto(id, customerName, productName, quantity, price, totalAmount, status, createdAt);
		}
	}
}
