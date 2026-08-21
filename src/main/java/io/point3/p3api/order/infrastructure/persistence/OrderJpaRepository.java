package io.point3.p3api.order.infrastructure.persistence;

import io.point3.p3api.order.domain.entity.Order;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<Order, UUID> {
  Optional<Order> findByIdAndBuyerUserId(UUID orderId, UUID buyerUserId);

  Optional<Order> findByIdAndStoreId(UUID orderId, UUID storeId);

  Optional<Order> findByPaymentAttemptId(UUID paymentAttemptId);

  List<Order> findAllByBuyerUserIdOrderByCreatedAtDesc(UUID buyerUserId);

  List<Order> findAllByStoreIdOrderByCreatedAtDesc(UUID storeId);
}
