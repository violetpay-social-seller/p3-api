package io.point3.p3api.order.application.state;

import java.util.Objects;
import java.util.UUID;

public record RequestOrderCancelCommand(UUID orderId, UUID buyerUserId, String reason) {

  public static RequestOrderCancelCommand of(UUID orderId, UUID buyerUserId, String reason) {
    Objects.requireNonNull(orderId, "orderId");
    Objects.requireNonNull(buyerUserId, "buyerUserId");
    Objects.requireNonNull(reason, "reason");

    return new RequestOrderCancelCommand(orderId, buyerUserId, reason);
  }
}
