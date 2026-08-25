package io.point3.p3api.payment.controller.response;

import io.point3.p3api.payment.application.result.PaymentAttemptResult;
import io.point3.p3api.payment.domain.type.PaymentAttemptStatus;
import java.time.Instant;
import java.util.UUID;

public record PaymentAttemptResponse(
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

  public static PaymentAttemptResponse from(PaymentAttemptResult result) {
    return new PaymentAttemptResponse(
        result.paymentAttemptId(),
        result.confirmationId(),
        result.sessionId(),
        result.amount(),
        result.status(),
        result.failureCode(),
        result.createdAt(),
        result.completedAt(),
        result.expiresAt(),
        result.expired());
  }
}
