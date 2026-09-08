package io.point3.p3api.order.controller.response;

import io.point3.p3api.order.application.option.OrderOptionRowResolver;
import io.point3.p3api.order.domain.entity.OrderConfirmation;
import io.point3.p3api.order.domain.type.OrderConfirmationStatus;
import java.time.Instant;
import java.util.List;
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
    String confirmedOptionPrices,
    String additionalItems,
    List<OrderOptionRowResponse> optionRows,
    String sellerNote,
    OrderConfirmationStatus status,
    Instant sentAt,
    Instant revisionRequestedAt,
    Instant buyerViewedAt,
    UUID replacedByConfirmationId,
    Instant createdAt) {

  public OrderConfirmationDetailResponse {
    optionRows = List.copyOf(optionRows);
  }

  public static OrderConfirmationDetailResponse from(
      OrderConfirmation confirmation, OrderOptionRowResolver optionRowResolver) {
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
        confirmation.getConfirmedOptionPrices(),
        confirmation.getAdditionalItems(),
        optionRowResolver.fromConfirmation(confirmation).stream()
            .map(OrderOptionRowResponse::from)
            .toList(),
        confirmation.getSellerNote(),
        confirmation.getStatus(),
        confirmation.getSentAt(),
        confirmation.getRevisionRequestedAt(),
        confirmation.getBuyerViewedAt(),
        confirmation.getReplacedByConfirmationId(),
        confirmation.getCreatedAt());
  }

  @Override
  public List<OrderOptionRowResponse> optionRows() {
    return List.copyOf(optionRows);
  }
}
