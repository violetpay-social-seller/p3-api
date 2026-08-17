package io.point3.p3api.order.controller.response;

import io.point3.p3api.order.domain.entity.OrderConfirmation;
import io.point3.p3api.order.domain.type.OrderConfirmationStatus;
import java.time.Instant;
import java.util.UUID;

public record OrderConfirmationDetailResponse(
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
    Instant revisionRequestedAt,
    UUID replacedByConfirmationId,
    Instant createdAt) {

  public static OrderConfirmationDetailResponse from(OrderConfirmation confirmation) {
    return new OrderConfirmationDetailResponse(
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
        confirmation.getRevisionRequestedAt(),
        confirmation.getReplacedByConfirmationId(),
        confirmation.getCreatedAt());
  }
}
