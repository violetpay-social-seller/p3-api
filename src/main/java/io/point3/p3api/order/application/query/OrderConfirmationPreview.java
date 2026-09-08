package io.point3.p3api.order.application.query;

import io.point3.p3api.order.application.price.OrderConfirmationPriceCalculator.UnconfirmedOption;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderConfirmationPreview(
    UUID orderFormSubmissionId,
    String confirmationTitle,
    Instant pickupAt,
    String fixedOrderSummary,
    long baseAmount,
    boolean inquiryRequired,
    boolean requiresManualAmount,
    List<UnconfirmedOption> unconfirmedOptions) {

  public OrderConfirmationPreview {
    unconfirmedOptions = List.copyOf(unconfirmedOptions);
  }

  @Override
  public List<UnconfirmedOption> unconfirmedOptions() {
    return List.copyOf(unconfirmedOptions);
  }
}
