package io.point3.p3api.order.application.state;

import java.util.Objects;
import java.util.UUID;

public record CompleteManualOrderRefundCommand(
    UUID orderId, UUID refundId, UUID storeId, UUID sellerUserId) {

  public static CompleteManualOrderRefundCommand of(
      UUID orderId, UUID refundId, UUID storeId, UUID sellerUserId) {
    Objects.requireNonNull(orderId, "orderId");
    Objects.requireNonNull(refundId, "refundId");
    Objects.requireNonNull(storeId, "storeId");
    Objects.requireNonNull(sellerUserId, "sellerUserId");

    return new CompleteManualOrderRefundCommand(orderId, refundId, storeId, sellerUserId);
  }
}
