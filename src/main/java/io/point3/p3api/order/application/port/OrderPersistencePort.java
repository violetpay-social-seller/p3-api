package io.point3.p3api.order.application.port;

import io.point3.p3api.order.domain.entity.Order;
import io.point3.p3api.order.domain.type.OrderStatus;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderPersistencePort {
  Order save(Order order);

  Optional<Order> findById(UUID orderId);

  Optional<Order> findByIdAndBuyerUserId(UUID orderId, UUID buyerUserId);

  Optional<Order> findByIdAndStoreId(UUID orderId, UUID storeId);

  Optional<Order> findByPaymentAttemptId(UUID paymentAttemptId);

  List<Order> findAllByBuyerUserId(UUID buyerUserId);

  List<Order> findAllByStoreId(UUID storeId);

  List<Order> findCalendarOrders(UUID storeId, Instant startInclusive, Instant endExclusive);

  List<Order> findCalendarOrdersByStatus(
      UUID storeId, OrderStatus status, Instant startInclusive, Instant endExclusive);

  long sumSucceededPaymentAmount(UUID storeId, Instant startInclusive, Instant endExclusive);

  long countByStoreIdAndStatus(UUID storeId, OrderStatus status);

  long countByStoreIdAndStatuses(UUID storeId, Collection<OrderStatus> statuses);
}
