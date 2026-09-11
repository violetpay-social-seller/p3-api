package io.point3.p3api.order.application.state;

import java.util.Objects;
import java.util.UUID;

public record RefreshOrderRefundCommand(UUID orderId, UUID storeId, UUID sellerUserId) {

  public static RefreshOrderRefundCommand of(UUID orderId, UUID storeId, UUID sellerUserId) {
    Objects.requireNonNull(orderId, "orderId");
    Objects.requireNonNull(storeId, "storeId");
    Objects.requireNonNull(sellerUserId, "sellerUserId");
    return new RefreshOrderRefundCommand(orderId, storeId, sellerUserId);
  }
}
