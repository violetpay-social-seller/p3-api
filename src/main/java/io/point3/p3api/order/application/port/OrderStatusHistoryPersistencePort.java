package io.point3.p3api.order.application.port;

import io.point3.p3api.order.domain.entity.OrderStatusHistory;
import java.util.List;
import java.util.UUID;

public interface OrderStatusHistoryPersistencePort {

  OrderStatusHistory save(OrderStatusHistory history);

  List<OrderStatusHistory> findAllByOrderId(UUID orderId);
}
