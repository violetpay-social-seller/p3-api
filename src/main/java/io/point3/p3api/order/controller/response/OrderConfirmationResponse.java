package io.point3.p3api.order.controller.response;

import io.point3.p3api.chat.domain.type.ChatTimelineItemType;
import io.point3.p3api.order.application.option.OrderOptionRowResolver;
import io.point3.p3api.order.application.result.SendOrderConfirmationResult;
import io.point3.p3api.order.application.result.SendOrderConfirmationResult.ChatTimelineItemSnapshot;
import io.point3.p3api.order.application.result.SendOrderConfirmationResult.OrderConfirmationSnapshot;
import io.point3.p3api.order.domain.type.OrderConfirmationStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderConfirmationResponse(
    UUID confirmationId,
    UUID inquiryId,
    UUID orderFormSubmissionId,
    String confirmationTitle,
    String summaryText,
    long amount,
    Instant pickupAt,
    String storeNameSnapshot,
    String orderSummary,
    String additionalItems,
    List<OrderOptionRowResponse> optionRows,
    String sellerNote,
    OrderConfirmationStatus status,
    Instant sentAt,
    UUID timelineEventId,
    ChatTimelineItemType timelineEventType,
    Instant timelineEventCreatedAt) {

  public OrderConfirmationResponse {
    optionRows = List.copyOf(optionRows);
  }

  public static OrderConfirmationResponse from(
      SendOrderConfirmationResult result, OrderOptionRowResolver optionRowResolver) {
    OrderConfirmationSnapshot confirmation = result.orderConfirmation();
    ChatTimelineItemSnapshot timelineItem = result.chatTimelineItem();

    return new OrderConfirmationResponse(
        confirmation.id(),
        confirmation.inquiryId(),
        confirmation.orderFormSubmissionId(),
        confirmation.menuName(),
        confirmation.optionSummary(),
        confirmation.amount(),
        confirmation.pickupAt(),
        confirmation.storeNameSnapshot(),
        confirmation.orderSummary(),
        confirmation.additionalItems(),
        optionRowResolver
            .fromConfirmation(confirmation.orderSummary(), confirmation.additionalItems())
            .stream()
            .map(OrderOptionRowResponse::from)
            .toList(),
        confirmation.sellerNote(),
        confirmation.status(),
        confirmation.sentAt(),
        timelineItem.id(),
        timelineItem.type(),
        timelineItem.createdAt());
  }

  @Override
  public List<OrderOptionRowResponse> optionRows() {
    return List.copyOf(optionRows);
  }
}
