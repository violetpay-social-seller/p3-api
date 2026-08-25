package io.point3.p3api.payment.controller.response;

import io.point3.p3api.payment.application.result.RefundResult;
import io.point3.p3api.payment.domain.type.RefundStatus;
import java.time.Instant;
import java.util.UUID;

public record RefundResponse(
    UUID refundId,
    UUID orderId,
    UUID paymentAttemptId,
    UUID requestedBy,
    long amount,
    String reason,
    RefundStatus status,
    Instant createdAt,
    Instant completedAt) {

  public static RefundResponse from(RefundResult result) {
    return new RefundResponse(
        result.refundId(),
        result.orderId(),
        result.paymentAttemptId(),
        result.requestedBy(),
        result.amount(),
        result.reason(),
        result.status(),
        result.createdAt(),
        result.completedAt());
  }
}
