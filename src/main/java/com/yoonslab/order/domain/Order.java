package com.yoonslab.order.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문(Order) 엔티티 - 대용량 데이터 처리 데모용
 */
@Entity
@Table(name = "orders", indexes = {
		@Index(name = "idx_orders_created_at", columnList = "createdAt"),
		@Index(name = "idx_orders_status", columnList = "status"),
		@Index(name = "idx_orders_customer_name", columnList = "customerName")
})
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String customerName;

	@Column(nullable = false, length = 200)
	private String productName;

	@Column(nullable = false)
	private Integer quantity;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal price;

	@Column(nullable = false, precision = 14, scale = 2)
	private BigDecimal totalAmount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OrderStatus status;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Column
	private LocalDateTime updatedAt;

	public enum OrderStatus {
		PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
	}

	public Order() {
	}

	public Order(Long id, String customerName, String productName, Integer quantity,
				 BigDecimal price, BigDecimal totalAmount, OrderStatus status,
				 LocalDateTime createdAt, LocalDateTime updatedAt) {
		this.id = id;
		this.customerName = customerName;
		this.productName = productName;
		this.quantity = quantity;
		this.price = price;
		this.totalAmount = totalAmount;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
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

	public OrderStatus getStatus() { return status; }
	public void setStatus(OrderStatus status) { this.status = status; }

	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

	// Static builder

	public static OrderBuilder builder() { return new OrderBuilder(); }

	public static class OrderBuilder {
		private Long id;
		private String customerName;
		private String productName;
		private Integer quantity;
		private BigDecimal price;
		private BigDecimal totalAmount;
		private OrderStatus status;
		private LocalDateTime createdAt;
		private LocalDateTime updatedAt;

		public OrderBuilder id(Long id) { this.id = id; return this; }
		public OrderBuilder customerName(String customerName) { this.customerName = customerName; return this; }
		public OrderBuilder productName(String productName) { this.productName = productName; return this; }
		public OrderBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }
		public OrderBuilder price(BigDecimal price) { this.price = price; return this; }
		public OrderBuilder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
		public OrderBuilder status(OrderStatus status) { this.status = status; return this; }
		public OrderBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
		public OrderBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

		public Order build() {
			return new Order(id, customerName, productName, quantity, price, totalAmount, status, createdAt, updatedAt);
		}
	}

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
		this.totalAmount = this.price.multiply(BigDecimal.valueOf(this.quantity));
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
