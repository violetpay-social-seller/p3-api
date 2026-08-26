package io.point3.p3api.operator.application.result;

import io.point3.p3api.payment.domain.entity.PaymentAttempt;
import io.point3.p3api.payment.domain.type.PaymentAttemptStatus;
import java.time.Instant;
import java.util.UUID;

public record OperatorPaymentAttemptResult(
    UUID id,
    UUID confirmationId,
    UUID payerUserId,
    String point3SessionId,
    String payerId,
    long amount,
    PaymentAttemptStatus status,
    String failureCode,
    Instant createdAt,
    Instant completedAt,
    Instant expiresAt) {

  public static OperatorPaymentAttemptResult from(PaymentAttempt paymentAttempt) {
    return new OperatorPaymentAttemptResult(
        paymentAttempt.getId(),
        paymentAttempt.getConfirmationId(),
        paymentAttempt.getPayerUserId(),
        paymentAttempt.getPoint3SessionId(),
        paymentAttempt.getPayerId(),
        paymentAttempt.getAmount(),
        paymentAttempt.getStatus(),
        paymentAttempt.getFailureCode(),
        paymentAttempt.getCreatedAt(),
        paymentAttempt.getCompletedAt(),
        paymentAttempt.getExpiresAt());
  }
}
