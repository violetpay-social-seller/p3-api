package io.point3.p3api.order.application.port;

import io.point3.p3api.order.domain.entity.Order;
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
}
