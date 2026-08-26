package io.point3.p3api.order.infrastructure.persistence;

import io.point3.p3api.order.domain.entity.OrderStatusHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusHistoryJpaRepository extends JpaRepository<OrderStatusHistory, UUID> {

  List<OrderStatusHistory> findAllByOrderIdOrderByCreatedAtDesc(UUID orderId);
}
