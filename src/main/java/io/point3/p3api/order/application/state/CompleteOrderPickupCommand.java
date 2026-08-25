package io.point3.p3api.order.application.state;

import java.util.Objects;
import java.util.UUID;

public record CompleteOrderPickupCommand(UUID orderId, UUID storeId) {

  public static CompleteOrderPickupCommand of(UUID orderId, UUID storeId) {
    Objects.requireNonNull(orderId, "orderId");
    Objects.requireNonNull(storeId, "storeId");

    return new CompleteOrderPickupCommand(orderId, storeId);
  }
}
