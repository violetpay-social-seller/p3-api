package io.point3.p3api.order.application.state;

import java.util.Objects;
import java.util.UUID;

public record RequestOrderRefundCommand(UUID orderId, UUID buyerUserId, String reason) {
  private static final String DEFAULT_REASON = "구매자 취소 요청";

  public static RequestOrderRefundCommand of(UUID orderId, UUID buyerUserId, String reason) {
    Objects.requireNonNull(orderId, "orderId");
    Objects.requireNonNull(buyerUserId, "buyerUserId");
    return new RequestOrderRefundCommand(orderId, buyerUserId, normalizeReason(reason));
  }

  private static String normalizeReason(String reason) {
    if (reason == null || reason.isBlank()) {
      return DEFAULT_REASON;
    }
    return reason;
  }
}
