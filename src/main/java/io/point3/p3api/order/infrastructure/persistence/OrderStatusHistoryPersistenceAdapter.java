package io.point3.p3api.order.infrastructure.persistence;

import io.point3.p3api.order.application.port.OrderStatusHistoryPersistencePort;
import io.point3.p3api.order.domain.entity.OrderStatusHistory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderStatusHistoryPersistenceAdapter implements OrderStatusHistoryPersistencePort {

  private final OrderStatusHistoryJpaRepository orderStatusHistoryJpaRepository;

  @Override
  public OrderStatusHistory save(OrderStatusHistory history) {
    return orderStatusHistoryJpaRepository.save(history);
  }

  @Override
  public List<OrderStatusHistory> findAllByOrderId(UUID orderId) {
    return orderStatusHistoryJpaRepository.findAllByOrderIdOrderByCreatedAtDesc(orderId);
  }
}
