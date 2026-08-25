package io.point3.p3api.order.infrastructure.persistence;

import io.point3.p3api.order.domain.entity.Order;
import io.point3.p3api.order.domain.type.OrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
      select o.pickupAt as pickupAt, count(o) as orderCount
      from Order o
      where o.storeId = :storeId
        and o.pickupAt >= :fromInclusive
        and o.pickupAt < :toExclusive
        and o.status in :statuses
      group by o.pickupAt
      """)
  List<PickupAtOrderCount> countByStoreIdAndPickupAtBetween(
      @Param("storeId") UUID storeId,
      @Param("fromInclusive") Instant fromInclusive,
      @Param("toExclusive") Instant toExclusive,
      @Param("statuses") Set<OrderStatus> statuses);

  interface PickupAtOrderCount {

    Instant getPickupAt();

    long getOrderCount();
  }
}
