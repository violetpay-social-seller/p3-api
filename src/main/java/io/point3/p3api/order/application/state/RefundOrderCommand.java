package io.point3.p3api.order.application.state;

import java.util.Objects;
import java.util.UUID;

public record RefundOrderCommand(UUID orderId, UUID storeId, UUID sellerUserId, String reason) {

  public static RefundOrderCommand of(
      UUID orderId, UUID storeId, UUID sellerUserId, String reason) {
    Objects.requireNonNull(orderId, "orderId");
    Objects.requireNonNull(storeId, "storeId");
    Objects.requireNonNull(sellerUserId, "sellerUserId");
    Objects.requireNonNull(reason, "reason");

    return new RefundOrderCommand(orderId, storeId, sellerUserId, reason);
  }
}
