package io.point3.p3api.order.controller.response;

import io.point3.p3api.chat.domain.entity.ChatTimelineItem;
import io.point3.p3api.chat.domain.type.ChatTimelineItemType;
import io.point3.p3api.order.application.result.SendOrderConfirmationResult;
import io.point3.p3api.order.domain.entity.OrderConfirmation;
import io.point3.p3api.order.domain.type.OrderConfirmationStatus;
import java.time.Instant;
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
        String sellerNote,
        OrderConfirmationStatus status,
        Instant sentAt,
        UUID timelineEventId,
        ChatTimelineItemType timelineEventType,
        Instant timelineEventCreatedAt) {

    public static OrderConfirmationResponse from(SendOrderConfirmationResult result) {
        OrderConfirmation confirmation = result.orderConfirmation();
        ChatTimelineItem timelineItem = result.chatTimelineItem();

        return new OrderConfirmationResponse(
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
                confirmation.getSentAt(),
                timelineItem.getId(),
                timelineItem.getType(),
                timelineItem.getCreatedAt());
    }
}