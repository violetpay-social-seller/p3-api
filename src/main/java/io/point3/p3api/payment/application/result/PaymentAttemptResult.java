package io.point3.p3api.payment.application.result;

import io.point3.p3api.payment.domain.entity.PaymentAttempt;
import io.point3.p3api.payment.domain.type.PaymentAttemptStatus;
import java.time.Instant;
import java.util.UUID;

public record PaymentAttemptResult(
    UUID paymentAttemptId,
    UUID confirmationId,
    String sessionId,
    long amount,
    PaymentAttemptStatus status,
    String failureCode,
    Instant createdAt,
    Instant completedAt,
    Instant expiresAt,
    boolean expired) {

  public static PaymentAttemptResult from(PaymentAttempt paymentAttempt, Instant now) {
    return new PaymentAttemptResult(
        paymentAttempt.getId(),
        paymentAttempt.getConfirmationId(),
        paymentAttempt.getPoint3SessionId(),
        paymentAttempt.getAmount(),
        paymentAttempt.getStatus(),
        paymentAttempt.getFailureCode(),
        paymentAttempt.getCreatedAt(),
        paymentAttempt.getCompletedAt(),
        paymentAttempt.getExpiresAt(),
        isSessionExpired(paymentAttempt, now));
  }

  private static boolean isSessionExpired(PaymentAttempt paymentAttempt, Instant now) {
    boolean expirable = paymentAttempt.getStatus() == PaymentAttemptStatus.READY
        || paymentAttempt.getStatus() == PaymentAttemptStatus.IN_PROGRESS;

    return expirable && now.isAfter(paymentAttempt.getExpiresAt());
  }
}
