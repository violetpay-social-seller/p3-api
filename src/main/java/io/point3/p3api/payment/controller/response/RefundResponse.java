package io.point3.p3api.payment.controller.response;

import io.point3.p3api.payment.application.result.RefundResult;
import io.point3.p3api.payment.domain.type.RefundOutcome;
import io.point3.p3api.payment.domain.type.RefundStatus;
import java.time.Instant;
import java.util.UUID;

public record RefundResponse(
    UUID refundId,
    UUID orderId,
    UUID paymentAttemptId,
    UUID requestedBy,
    long amount,
    int refundRate,
    String reason,
    RefundStatus status,
    RefundOutcome outcome,
    boolean retryable,
    String providerRefundId,
    String failureCode,
    String failureMessage,
    String failureDetails,
    Instant createdAt,
    Instant completedAt) {

  public static RefundResponse from(RefundResult result) {
    return new RefundResponse(
        result.refundId(),
        result.orderId(),
        result.paymentAttemptId(),
        result.requestedBy(),
        result.amount(),
        result.refundRate(),
        result.reason(),
        result.status(),
        result.outcome(),
        result.retryable(),
        result.providerRefundId(),
        result.failureCode(),
        result.failureMessage(),
        result.failureDetails(),
        result.createdAt(),
        result.completedAt());
  }
}
