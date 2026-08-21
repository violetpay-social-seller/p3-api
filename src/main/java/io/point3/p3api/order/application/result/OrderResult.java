package io.point3.p3api.order.application.result;

import io.point3.p3api.order.domain.entity.Order;
import io.point3.p3api.order.domain.type.OrderStatus;
import java.time.Instant;
import java.util.UUID;

public record OrderResult(
    UUID id,
    UUID storeId,
    UUID buyerUserId,
    UUID inquiryId,
    UUID confirmationId,
    String orderNumber,
    String menuName,
    String optionSummary,
    long paidAmount,
    Instant pickupAt,
    OrderStatus status,
    Instant cancelRequestedAt,
    String cancelReason,
    Instant createdAt,
    Instant updatedAt) {
  public static OrderResult from(Order order) {
    return new OrderResult(
        order.getId(),
        order.getStoreId(),
        order.getBuyerUserId(),
        order.getInquiryId(),
        order.getConfirmationId(),
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
