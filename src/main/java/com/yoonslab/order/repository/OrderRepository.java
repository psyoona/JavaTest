package com.yoonslab.order.repository;

import com.yoonslab.order.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
 * 3. Offset 기반 페이징 (번호 페이지네이션)
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    // ──────────────────────────────────────────────
    // 1) Cursor(Keyset) 기반 페이징 - 대용량 데이터에서 가장 효율적
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
    // 3) 오프셋 기반 페이징 (번호 페이지네이션용)
    // ※ 성능 주의: LIKE %:customerName% 는 선행 와일드카드로 인해 인덱스를 사용하지 못함.
    //   데이터 수백만 건 이상 환경에서는 PostgreSQL pg_trgm 확장(GIN 인덱스)을 고려할 것.
    // ──────────────────────────────────────────────

    @Query("""
            SELECT o FROM Order o
            WHERE (:customerName IS NULL OR o.customerName LIKE %:customerName%)
              AND (:status IS NULL OR o.status = :status)
            ORDER BY o.id ASC
            """)
    Page<Order> findPageWithCondition(
            @Param("customerName") String customerName,
            @Param("status") Order.OrderStatus status,
            Pageable pageable);

    // ──────────────────────────────────────────────
    // 4) 카운트 쿼리 (필요 시에만 사용)
    // ──────────────────────────────────────────────

    @Query("""
            SELECT COUNT(o) FROM Order o
            WHERE (:customerName IS NULL OR o.customerName LIKE %:customerName%)
              AND (:status IS NULL OR o.status = :status)
            """)
    long countWithCondition(@Param("customerName") String customerName,
                            @Param("status") Order.OrderStatus status);
}
