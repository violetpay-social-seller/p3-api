package io.point3.p3api.order.application.state;

import java.util.Objects;
import java.util.UUID;

public record RefundOrderCommand(UUID orderId, UUID storeId, UUID sellerUserId, String reason) {
  private static final String DEFAULT_REASON = "판매자 환불 처리";

  public static RefundOrderCommand of(
      UUID orderId, UUID storeId, UUID sellerUserId, String reason) {
    Objects.requireNonNull(orderId, "orderId");
    Objects.requireNonNull(storeId, "storeId");
    Objects.requireNonNull(sellerUserId, "sellerUserId");

    return new RefundOrderCommand(orderId, storeId, sellerUserId, normalizeReason(reason));
  }

  private static String normalizeReason(String reason) {
    if (reason == null || reason.isBlank()) {
      return DEFAULT_REASON;
    }
    return reason;
  }
}
