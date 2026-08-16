package io.point3.p3api.order.application.result;

import io.point3.p3api.chat.domain.entity.ChatTimelineItem;
import io.point3.p3api.order.domain.entity.OrderConfirmation;

public record SendOrderConfirmationResult(
    OrderConfirmation orderConfirmation, ChatTimelineItem chatTimelineItem) {

  public static SendOrderConfirmationResult of(
      OrderConfirmation orderConfirmation, ChatTimelineItem chatTimelineItem) {
    return new SendOrderConfirmationResult(orderConfirmation, chatTimelineItem);
  }
}
