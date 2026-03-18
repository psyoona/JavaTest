package com.yoonslab.order.repository;

import com.yoonslab.order.domain.Order;
import com.yoonslab.order.dto.OrderDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * JDBC 기반 대용량 데이터 처리 Repository
 *
 * PostgreSQL의 서버 사이드 커서를 직접 활용하여
 * 수백만 건의 데이터를 메모리 부담 없이 처리
 */
@Repository
public class OrderJdbcRepository {

    private static final Logger log = Logger.getLogger(OrderJdbcRepository.class.getName());

    private final JdbcTemplate jdbcTemplate;

    public OrderJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * PostgreSQL 서버 사이드 커서를 이용한 대용량 데이터 스트리밍 처리
     */
    public void streamAllOrders(Consumer<OrderDto> consumer) {
        jdbcTemplate.setFetchSize(500);

        jdbcTemplate.query(
                "SELECT id, customer_name, product_name, quantity, price, total_amount, status, created_at " +
                        "FROM orders ORDER BY id",
                (ResultSet rs) -> {
                    while (rs.next()) {
                        consumer.accept(mapRowToDto(rs));
                    }
                }
        );
    }

    /**
     * 배치 단위로 대용량 데이터 조회
     */
    public void batchProcessOrders(int batchSize, Consumer<List<OrderDto>> consumer) {
        jdbcTemplate.setFetchSize(batchSize);

        final List<OrderDto> batch = new ArrayList<>(batchSize);

        jdbcTemplate.query(
                "SELECT id, customer_name, product_name, quantity, price, total_amount, status, created_at " +
                        "FROM orders ORDER BY id",
                (ResultSet rs) -> {
                    while (rs.next()) {
                        batch.add(mapRowToDto(rs));

                        if (batch.size() >= batchSize) {
                            consumer.accept(new ArrayList<>(batch));
                            batch.clear();
                        }
                    }
                    if (!batch.isEmpty()) {
                        consumer.accept(new ArrayList<>(batch));
                        batch.clear();
                    }
                }
        );
    }

    /**
     * JDBC 배치 INSERT - 대량 데이터 삽입 시 사용
     */
    public void batchInsert(List<Order> orders) {
        String sql = "INSERT INTO orders (customer_name, product_name, quantity, price, total_amount, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, orders, 1000,
                (ps, order) -> {
                    ps.setString(1, order.getCustomerName());
                    ps.setString(2, order.getProductName());
                    ps.setInt(3, order.getQuantity());
                    ps.setBigDecimal(4, order.getPrice());
                    ps.setBigDecimal(5, order.getTotalAmount());
                    ps.setString(6, order.getStatus().name());
                    ps.setObject(7, order.getCreatedAt());
                });

        log.info("Batch inserted " + orders.size() + " orders");
    }

    private OrderDto mapRowToDto(ResultSet rs) throws SQLException {
        return OrderDto.builder()
                .id(rs.getLong("id"))
                .customerName(rs.getString("customer_name"))
                .productName(rs.getString("product_name"))
                .quantity(rs.getInt("quantity"))
                .price(rs.getBigDecimal("price"))
                .totalAmount(rs.getBigDecimal("total_amount"))
                .status(rs.getString("status"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    }
}
