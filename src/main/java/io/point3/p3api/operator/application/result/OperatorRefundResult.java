package io.point3.p3api.operator.application.result;

import io.point3.p3api.payment.domain.entity.Refund;
import io.point3.p3api.payment.domain.type.RefundStatus;
import java.time.Instant;
import java.util.UUID;

public record OperatorRefundResult(
    UUID id,
    UUID orderId,
    UUID paymentAttemptId,
    UUID requestedBy,
    long amount,
    String reason,
    RefundStatus status,
    Instant createdAt,
    Instant completedAt) {

  public static OperatorRefundResult from(Refund refund) {
    return new OperatorRefundResult(
        refund.getId(),
        refund.getOrderId(),
        refund.getPaymentAttemptId(),
        refund.getRequestedBy(),
        refund.getAmount(),
        refund.getReason(),
        refund.getStatus(),
        refund.getCreatedAt(),
        refund.getCompletedAt());
  }
}
