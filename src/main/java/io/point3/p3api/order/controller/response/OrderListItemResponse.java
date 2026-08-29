package io.point3.p3api.order.controller.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.order.application.result.OrderResult;
import io.point3.p3api.order.domain.type.OrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderListItemResponse(
    UUID id,
    UUID storeId,
    UUID buyerUserId,
    UUID inquiryId,
    UUID confirmationId,
    String orderNumber,
    String menuName,
    String optionSummary,
    List<UUID> startReferenceAssets,
    long paidAmount,
    Instant pickupAt,
    OrderStatus status,
    Instant cancelRequestedAt,
    String cancelReason,
    Instant createdAt,
    Instant updatedAt) {

  private static final TypeReference<List<UUID>> ASSET_IDS = new TypeReference<>() {};

  public OrderListItemResponse {
    startReferenceAssets = List.copyOf(startReferenceAssets);
  }

  public static OrderListItemResponse from(OrderResult result, ObjectMapper objectMapper) {
    return new OrderListItemResponse(
        result.id(),
        result.storeId(),
        result.buyerUserId(),
        result.inquiryId(),
        result.confirmationId(),
        result.orderNumber(),
        result.menuName(),
        result.optionSummary(),
        readStartReferenceAssets(result.startReferenceAssets(), objectMapper),
        result.paidAmount(),
        result.pickupAt(),
        result.status(),
        result.cancelRequestedAt(),
        result.cancelReason(),
        result.createdAt(),
        result.updatedAt());
  }

  @Override
  public List<UUID> startReferenceAssets() {
    return List.copyOf(startReferenceAssets);
  }

  private static List<UUID> readStartReferenceAssets(
      String startReferenceAssets, ObjectMapper objectMapper) {
    if (startReferenceAssets == null || startReferenceAssets.isBlank()) {
      return List.of();
    }

    try {
      return objectMapper.readValue(startReferenceAssets, ASSET_IDS);
    } catch (JsonProcessingException e) {
      throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
  }
}
