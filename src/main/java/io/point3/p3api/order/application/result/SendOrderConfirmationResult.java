package io.point3.p3api.order.application.result;

import io.point3.p3api.chat.domain.entity.ChatTimelineItem;
import io.point3.p3api.chat.domain.type.ChatTimelineItemType;
import io.point3.p3api.order.domain.entity.OrderConfirmation;
import io.point3.p3api.order.domain.type.OrderConfirmationStatus;
import java.time.Instant;
import java.util.UUID;

public record SendOrderConfirmationResult(
    OrderConfirmationSnapshot orderConfirmation, ChatTimelineItemSnapshot chatTimelineItem) {

  public static SendOrderConfirmationResult of(
      OrderConfirmation orderConfirmation, ChatTimelineItem chatTimelineItem) {
    return new SendOrderConfirmationResult(
        OrderConfirmationSnapshot.from(orderConfirmation),
        ChatTimelineItemSnapshot.from(chatTimelineItem));
  }

  public record OrderConfirmationSnapshot(
      UUID id,
      UUID inquiryId,
      UUID orderFormSubmissionId,
      String menuName,
      String optionSummary,
      long amount,
      Instant pickupAt,
      String storeNameSnapshot,
      String orderSummary,
      String additionalItems,
      String sellerNote,
      OrderConfirmationStatus status,
      Instant sentAt) {

    static OrderConfirmationSnapshot from(OrderConfirmation confirmation) {
      return new OrderConfirmationSnapshot(
          confirmation.getId(),
          confirmation.getInquiryId(),
          confirmation.getOrderFormSubmissionId(),
          confirmation.getMenuName(),
          confirmation.getOptionSummary(),
          confirmation.getAmount(),
          confirmation.getPickupAt(),
          confirmation.getStoreNameSnapshot(),
          confirmation.getOrderSummary(),
          confirmation.getAdditionalItems(),
          confirmation.getSellerNote(),
          confirmation.getStatus(),
          confirmation.getSentAt());
    }
  }

  public record ChatTimelineItemSnapshot(
      UUID id, ChatTimelineItemType type, UUID senderUserId, Instant createdAt) {

    static ChatTimelineItemSnapshot from(ChatTimelineItem timelineItem) {
      return new ChatTimelineItemSnapshot(
          timelineItem.getId(),
          timelineItem.getType(),
          timelineItem.getSenderUserId(),
          timelineItem.getCreatedAt());
    }
  }
}
