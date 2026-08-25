package io.point3.p3api.order.application.port;

import io.point3.p3api.order.domain.entity.Order;
import io.point3.p3api.order.domain.type.OrderStatus;
import io.point3.p3api.order.application.result.OrderPickupDateCount;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface OrderPersistencePort {
  Order save(Order order);

  Optional<Order> findById(UUID orderId);

  Optional<Order> findByIdAndBuyerUserId(UUID orderId, UUID buyerUserId);

  Optional<Order> findByIdAndStoreId(UUID orderId, UUID storeId);

  Optional<Order> findByPaymentAttemptId(UUID paymentAttemptId);

  List<Order> findAllByBuyerUserId(UUID buyerUserId);

  List<Order> findAllByStoreId(UUID storeId);

  List<OrderPickupDateCount> countByStoreIdAndPickupAtBetween(
      UUID storeId, Instant fromInclusive, Instant toExclusive, Set<OrderStatus> statuses);
}
