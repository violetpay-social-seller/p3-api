package io.point3.p3api.order.infrastructure.persistence;

import io.point3.p3api.order.domain.entity.Order;
import io.point3.p3api.order.domain.type.OrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderJpaRepository extends JpaRepository<Order, UUID> {
  Optional<Order> findByIdAndBuyerUserId(UUID orderId, UUID buyerUserId);

  Optional<Order> findByIdAndStoreId(UUID orderId, UUID storeId);

  Optional<Order> findByPaymentAttemptId(UUID paymentAttemptId);

  List<Order> findAllByBuyerUserIdOrderByCreatedAtDesc(UUID buyerUserId);

  List<Order> findAllByStoreIdOrderByCreatedAtDesc(UUID storeId);

  @Query("""
      select o
      from Order o
      where o.storeId = :storeId
        and o.pickupAt >= :startInclusive
        and o.pickupAt < :endExclusive
      order by o.pickupAt asc
      """)
  List<Order> findCalendarOrders(
      @Param("storeId") UUID storeId,
      @Param("startInclusive") Instant startInclusive,
      @Param("endExclusive") Instant endExclusive);

  @Query("""
      select o
      from Order o
      where o.storeId = :storeId
        and o.status = :status
        and o.pickupAt >= :startInclusive
        and o.pickupAt < :endExclusive
      order by o.pickupAt asc
      """)
  List<Order> findCalendarOrdersByStatus(
      @Param("storeId") UUID storeId,
      @Param("status") OrderStatus status,
      @Param("startInclusive") Instant startInclusive,
      @Param("endExclusive") Instant endExclusive);
}
