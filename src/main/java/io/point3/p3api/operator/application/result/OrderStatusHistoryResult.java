package io.point3.p3api.operator.application.result;

import io.point3.p3api.order.domain.entity.OrderStatusHistory;
import io.point3.p3api.order.domain.type.OrderStatus;
import java.time.Instant;
import java.util.UUID;

public record OrderStatusHistoryResult(
    UUID id,
    UUID orderId,
    OrderStatus previousStatus,
    OrderStatus newStatus,
    UUID changedBy,
    String reason,
    Instant createdAt) {

  public static OrderStatusHistoryResult from(OrderStatusHistory history) {
    return new OrderStatusHistoryResult(
        history.getId(),
        history.getOrderId(),
        history.getPreviousStatus(),
        history.getNewStatus(),
        history.getChangedBy(),
        history.getReason(),
        history.getCreatedAt());
  }
}
