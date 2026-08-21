package io.point3.p3api.order.controller.response;

import io.point3.p3api.order.application.result.OrderResult;
import io.point3.p3api.order.domain.type.OrderStatus;
import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
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
  public static OrderResponse from(OrderResult result) {
    return new OrderResponse(
        result.id(),
        result.storeId(),
        result.buyerUserId(),
        result.inquiryId(),
        result.confirmationId(),
        result.orderNumber(),
        result.menuName(),
        result.optionSummary(),
        result.paidAmount(),
        result.pickupAt(),
        result.status(),
        result.cancelRequestedAt(),
        result.cancelReason(),
        result.createdAt(),
        result.updatedAt());
  }
}
