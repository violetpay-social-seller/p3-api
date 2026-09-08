package io.point3.p3api.order.controller.response;

import io.point3.p3api.order.application.query.OrderConfirmationPreview;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderConfirmationPreviewResponse(
    UUID orderFormSubmissionId,
    String confirmationTitle,
    Instant pickupAt,
    String fixedOrderSummary,
    long baseAmount,
    boolean inquiryRequired,
    boolean requiresManualAmount,
    List<UnconfirmedOption> unconfirmedOptions) {

  public OrderConfirmationPreviewResponse {
    unconfirmedOptions = List.copyOf(unconfirmedOptions);
  }

  public static OrderConfirmationPreviewResponse from(OrderConfirmationPreview preview) {
    return new OrderConfirmationPreviewResponse(
        preview.orderFormSubmissionId(),
        preview.confirmationTitle(),
        preview.pickupAt(),
        preview.fixedOrderSummary(),
        preview.baseAmount(),
        preview.inquiryRequired(),
        preview.requiresManualAmount(),
        preview.unconfirmedOptions().stream().map(UnconfirmedOption::from).toList());
  }

  @Override
  public List<UnconfirmedOption> unconfirmedOptions() {
    return List.copyOf(unconfirmedOptions);
  }

  public record UnconfirmedOption(
      UUID optionGroupId,
      String optionValue,
      String label,
      String displayValue,
      String priceLabel) {

    private static UnconfirmedOption from(
        io.point3.p3api.order.application.price.OrderConfirmationPriceCalculator.UnconfirmedOption
            option) {
      return new UnconfirmedOption(
          option.optionGroupId(),
          option.optionValue(),
          option.label(),
          option.displayValue(),
          option.priceLabel());
    }
  }
}
