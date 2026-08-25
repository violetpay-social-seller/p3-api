package io.point3.p3api.payment.controller.response;

import io.point3.p3api.payment.application.result.PaymentCtaResult;
import io.point3.p3api.payment.application.result.PaymentCtaStatus;
import java.time.Instant;
import java.util.UUID;

public record PaymentCtaResponse(
    UUID inquiryId,
    UUID confirmationId,
    long amount,
    boolean canPay,
    PaymentCtaStatus status,
    String reason,
    Instant buyerViewedAt,
    PaymentAttemptResponse latestPaymentAttempt) {

  public static PaymentCtaResponse from(PaymentCtaResult result) {
    PaymentAttemptResponse latestPaymentAttempt = result.latestPaymentAttempt() == null
        ? null
        : PaymentAttemptResponse.from(result.latestPaymentAttempt());

    return new PaymentCtaResponse(
        result.inquiryId(),
        result.confirmationId(),
        result.amount(),
        result.canPay(),
        result.status(),
        result.reason(),
        result.buyerViewedAt(),
        latestPaymentAttempt);
  }
}
