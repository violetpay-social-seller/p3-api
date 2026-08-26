package io.point3.p3api.order.controller.response;

import io.point3.p3api.order.application.query.OrderConfirmationPreview;
import java.time.Instant;
import java.util.UUID;

public record OrderConfirmationPreviewResponse(
    UUID orderFormSubmissionId,
    String confirmationTitle,
    Instant pickupAt,
    String fixedOrderSummary,
    long baseAmount,
    boolean inquiryRequired) {

  public static OrderConfirmationPreviewResponse from(OrderConfirmationPreview preview) {
    return new OrderConfirmationPreviewResponse(
        preview.orderFormSubmissionId(),
        preview.confirmationTitle(),
        preview.pickupAt(),
        preview.fixedOrderSummary(),
        preview.baseAmount(),
        preview.inquiryRequired());
  }
}
