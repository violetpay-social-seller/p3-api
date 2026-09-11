package io.point3.p3api.order.controller.response;

import io.point3.p3api.order.application.result.OrderReferenceAssetResult;
import io.point3.p3api.order.application.result.OrderResult;
import io.point3.p3api.order.domain.type.OrderStatus;
import java.time.Instant;
import java.util.List;
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
    List<UUID> startReferenceAssets,
    List<ReferenceAssetResponse> referenceAssets,
    long paidAmount,
    Instant pickupAt,
    OrderStatus status,
    Instant refundRequestedAt,
    String refundReason,
    Instant createdAt,
    Instant updatedAt) {

  public OrderResponse {
    startReferenceAssets = List.copyOf(startReferenceAssets);
    referenceAssets = List.copyOf(referenceAssets);
  }

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
        result.referenceAssets().stream()
            .map(OrderReferenceAssetResult::assetId)
            .toList(),
        result.referenceAssets().stream().map(ReferenceAssetResponse::from).toList(),
        result.paidAmount(),
        result.pickupAt(),
        result.status(),
        result.refundRequestedAt(),
        result.refundReason(),
        result.createdAt(),
        result.updatedAt());
  }

  @Override
  public List<UUID> startReferenceAssets() {
    return List.copyOf(startReferenceAssets);
  }

  @Override
  public List<ReferenceAssetResponse> referenceAssets() {
    return List.copyOf(referenceAssets);
  }
}
