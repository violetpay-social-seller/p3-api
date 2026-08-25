package io.point3.p3api.payment.application.result;

import io.point3.p3api.payment.domain.entity.PaymentAttempt;
import io.point3.p3api.payment.domain.type.PaymentAttemptStatus;
import java.util.UUID;

public record PaymentCaptureResult(
    UUID paymentAttemptId,
    String sessionId,
    long amount,
    PaymentAttemptStatus status,
    UUID orderId,
    String failureCode) {

  public static PaymentCaptureResult of(PaymentAttempt paymentAttempt, UUID orderId) {
    return new PaymentCaptureResult(
        paymentAttempt.getId(),
        paymentAttempt.getPoint3SessionId(),
        paymentAttempt.getAmount(),
        paymentAttempt.getStatus(),
        orderId,
        paymentAttempt.getFailureCode());
  }
}
