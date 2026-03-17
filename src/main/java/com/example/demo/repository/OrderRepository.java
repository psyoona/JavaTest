package com.example.demo.repository;

import com.example.demo.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.stream.Stream;

import static org.hibernate.jpa.HibernateHints.*;

/**
 * 주문 Repository
 *
 * 대용량 데이터 처리를 위한 3가지 방식 제공:
 * 1. Cursor 기반 페이징 (Keyset Pagination)
 * 2. JDBC Stream (서버 사이드 커서)
 * 3. Batch Fetch
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    // ──────────────────────────────────────────────
    // 1) Cursor(Keyset) 기반 페이징 - 대용량 데이터에서 가장 효율적
    //    WHERE id > :cursor ORDER BY id LIMIT :size
    //    → offset 없이 인덱스 스캔만으로 다음 페이지 조회
    // ──────────────────────────────────────────────

    @Query("""
            SELECT o FROM Order o
            WHERE o.id > :cursor
            ORDER BY o.id ASC
            LIMIT :size
            """)
    List<Order> findByCursor(@Param("cursor") Long cursor,
                             @Param("size") int size);

    @Query("""
            SELECT o FROM Order o
            WHERE o.id > :cursor
              AND (:customerName IS NULL OR o.customerName LIKE %:customerName%)
              AND (:status IS NULL OR o.status = :status)
            ORDER BY o.id ASC
            LIMIT :size
            """)
    List<Order> findByCursorWithCondition(
            @Param("cursor") Long cursor,
            @Param("size") int size,
            @Param("customerName") String customerName,
            @Param("status") Order.OrderStatus status);

    // ──────────────────────────────────────────────
    // 2) Stream 기반 조회 - 서버 사이드 커서 사용
    //    PostgreSQL DECLARE CURSOR + FETCH FORWARD
    //    → 메모리에 전체 결과를 올리지 않고 한 행씩 처리
    // ──────────────────────────────────────────────

    @QueryHints(value = {
            @QueryHint(name = HINT_FETCH_SIZE, value = "500"),
            @QueryHint(name = HINT_CACHEABLE, value = "false"),
            @QueryHint(name = HINT_READ_ONLY, value = "true")
    })
    @Query("SELECT o FROM Order o ORDER BY o.id")
    Stream<Order> streamAll();

    @QueryHints(value = {
            @QueryHint(name = HINT_FETCH_SIZE, value = "500"),
            @QueryHint(name = HINT_CACHEABLE, value = "false"),
            @QueryHint(name = HINT_READ_ONLY, value = "true")
    })
    @Query("""
            SELECT o FROM Order o
            WHERE (:status IS NULL OR o.status = :status)
            ORDER BY o.id
            """)
    Stream<Order> streamByStatus(@Param("status") Order.OrderStatus status);

    // ──────────────────────────────────────────────
    // 3) 카운트 쿼리 (필요 시에만 사용)
    // ──────────────────────────────────────────────

    @Query("""
            SELECT COUNT(o) FROM Order o
            WHERE (:customerName IS NULL OR o.customerName LIKE %:customerName%)
              AND (:status IS NULL OR o.status = :status)
            """)
    long countWithCondition(@Param("customerName") String customerName,
                            @Param("status") Order.OrderStatus status);
}
