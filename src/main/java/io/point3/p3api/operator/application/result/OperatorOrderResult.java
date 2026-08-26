package io.point3.p3api.operator.application.result;

import io.point3.p3api.order.domain.entity.Order;
import io.point3.p3api.order.domain.type.OrderStatus;
import java.time.Instant;
import java.util.UUID;

public record OperatorOrderResult(
    UUID id,
    UUID storeId,
    UUID buyerUserId,
    UUID inquiryId,
    UUID confirmationId,
    UUID paymentAttemptId,
    String orderNumber,
    String menuNameSnapshot,
    String optionSummarySnapshot,
    long paidAmount,
    Instant pickupAt,
    OrderStatus status,
    Instant cancelRequestedAt,
    String cancelReason,
    Instant createdAt,
    Instant updatedAt) {

  public static OperatorOrderResult from(Order order) {
    return new OperatorOrderResult(
        order.getId(),
        order.getStoreId(),
        order.getBuyerUserId(),
        order.getInquiryId(),
        order.getConfirmationId(),
        order.getPaymentAttemptId(),
        order.getOrderNumber(),
        order.getMenuNameSnapshot(),
        order.getOptionSummarySnapshot(),
        order.getPaidAmount(),
        order.getPickupAt(),
        order.getStatus(),
        order.getCancelRequestedAt(),
        order.getCancelReason(),
        order.getCreatedAt(),
        order.getUpdatedAt());
  }
}
