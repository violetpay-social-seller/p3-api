package io.point3.p3api.order.application.query;

import java.time.Instant;
import java.util.UUID;

public record OrderConfirmationPreview(
    UUID orderFormSubmissionId,
    String confirmationTitle,
    Instant pickupAt,
    String fixedOrderSummary,
    long baseAmount,
    boolean inquiryRequired) {}
