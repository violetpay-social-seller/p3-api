package io.point3.p3api.payment.application.result;

import io.point3.p3api.payment.domain.entity.Refund;
import io.point3.p3api.payment.domain.type.RefundCompletionMethod;
import io.point3.p3api.payment.domain.type.RefundOutcome;
import io.point3.p3api.payment.domain.type.RefundStatus;
import java.time.Instant;
import java.util.UUID;

public record RefundResult(
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
    Instant completedAt,
    UUID completedBy,
    RefundCompletionMethod completionMethod,
    Instant failedAt) {

  public static RefundResult from(Refund refund) {
    return new RefundResult(
        refund.getId(),
        refund.getOrderId(),
        refund.getPaymentAttemptId(),
        refund.getRequestedBy(),
        refund.getAmount(),
        refund.getRefundRate(),
        refund.getReason(),
        refund.getStatus(),
        refund.getOutcome(),
        refund.isRetryableFailure(),
        refund.getProviderRefundId(),
        refund.getFailureCode(),
        refund.getFailureMessage(),
        refund.getFailureDetails(),
        refund.getCreatedAt(),
        refund.getCompletedAt(),
        refund.getCompletedBy(),
        refund.getCompletionMethod(),
        refund.getFailedAt());
  }
}
