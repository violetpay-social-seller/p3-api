package io.point3.p3api.payment.domain.entity;

import io.point3.p3api.payment.domain.type.RefundCompletionMethod;
import io.point3.p3api.payment.domain.type.RefundOutcome;
import io.point3.p3api.payment.domain.type.RefundStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "refunds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "order_id", nullable = false)
  private UUID orderId;

  @Column(name = "payment_attempt_id", nullable = false)
  private UUID paymentAttemptId;

  @Column(name = "requested_by", nullable = false)
  private UUID requestedBy;

  @Column(name = "amount", nullable = false)
  private long amount;

  @Column(name = "refund_rate", nullable = false)
  private int refundRate;

  @Column(name = "reason", columnDefinition = "text")
  private String reason;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private RefundStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "outcome", nullable = false, length = 30)
  private RefundOutcome outcome;

  @Column(name = "provider_refund_id", length = 128)
  private String providerRefundId;

  @Column(name = "failure_code", length = 100)
  private String failureCode;

  @Column(name = "failure_message", columnDefinition = "text")
  private String failureMessage;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "failure_details", columnDefinition = "jsonb")
  private String failureDetails;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "completed_at")
  private Instant completedAt;

  @Column(name = "failed_at")
  private Instant failedAt;

  @Column(name = "completed_by")
  private UUID completedBy;

  @Enumerated(EnumType.STRING)
  @Column(name = "completion_method", length = 30)
  private RefundCompletionMethod completionMethod;

  private Refund(
      UUID orderId,
      UUID paymentAttemptId,
      UUID requestedBy,
      long amount,
      int refundRate,
      String reason) {
    this.orderId = orderId;
    this.paymentAttemptId = paymentAttemptId;
    this.requestedBy = requestedBy;
    this.amount = amount;
    this.refundRate = refundRate;
    this.reason = reason;
    this.status = RefundStatus.REQUESTED;
    this.outcome = RefundOutcome.PROCESSING;
  }

  public static Refund create(
      UUID orderId, UUID paymentAttemptId, UUID requestedBy, long amount, String reason) {
    return create(orderId, paymentAttemptId, requestedBy, amount, 100, reason);
  }

  public static Refund create(
      UUID orderId,
      UUID paymentAttemptId,
      UUID requestedBy,
      long amount,
      int refundRate,
      String reason) {
    Objects.requireNonNull(orderId, "orderId");
    Objects.requireNonNull(paymentAttemptId, "paymentAttemptId");
    Objects.requireNonNull(requestedBy, "requestedBy");

    if (amount < 0) {
      throw new IllegalArgumentException("amount must be greater than or equal to 0");
    }
    if (refundRate < 0 || refundRate > 100) {
      throw new IllegalArgumentException("refundRate must be between 0 and 100");
    }

    return new Refund(orderId, paymentAttemptId, requestedBy, amount, refundRate, reason);
  }

  public void startProcessing() {
    if (status != RefundStatus.REQUESTED) {
      throw new IllegalStateException("Refund status transition is not allowed");
    }
    status = RefundStatus.PROCESSING;
    outcome = RefundOutcome.PROCESSING;
    clearFailure();
  }

  public void complete(Instant completedAt) {
    complete(null, completedAt);
  }

  public void complete(String providerRefundId, Instant completedAt) {
    complete(providerRefundId, null, RefundCompletionMethod.AUTOMATIC, true, true, completedAt);
  }

  public void completeAutomatically(
      String providerRefundId, UUID completedBy, Instant completedAt) {
    Objects.requireNonNull(completedBy, "completedBy");
    complete(
        providerRefundId, completedBy, RefundCompletionMethod.AUTOMATIC, true, true, completedAt);
  }

  public void completeZeroAmount(UUID completedBy, Instant completedAt) {
    Objects.requireNonNull(completedBy, "completedBy");
    complete(null, completedBy, RefundCompletionMethod.ZERO_AMOUNT, true, true, completedAt);
  }

  public void completeManually(UUID completedBy, Instant completedAt) {
    Objects.requireNonNull(completedBy, "completedBy");
    if (status != RefundStatus.FAILED || outcome != RefundOutcome.MANUAL_REQUIRED) {
      throw new IllegalStateException("Refund status transition is not allowed");
    }
    complete(null, completedBy, RefundCompletionMethod.MANUAL, false, false, completedAt);
  }

  public void keepProcessing(
      String providerRefundId, String failureCode, String failureMessage, String failureDetails) {
    this.status = RefundStatus.PROCESSING;
    this.outcome = RefundOutcome.PROCESSING;
    recordProviderResult(providerRefundId, failureCode, failureMessage, failureDetails);
  }

  public void fail() {
    fail(RefundOutcome.FAILED, null, null, null, null, Instant.now());
  }

  public void fail(
      RefundOutcome outcome,
      String providerRefundId,
      String failureCode,
      String failureMessage,
      String failureDetails,
      Instant failedAt) {
    if (outcome == RefundOutcome.COMPLETED || outcome == RefundOutcome.PROCESSING) {
      throw new IllegalArgumentException("outcome must be a failed outcome");
    }
    Objects.requireNonNull(failedAt, "failedAt");
    this.status = RefundStatus.FAILED;
    this.outcome = outcome;
    this.failedAt = failedAt;
    recordProviderResult(providerRefundId, failureCode, failureMessage, failureDetails);
  }

  public boolean isRetryableFailure() {
    return status == RefundStatus.FAILED && outcome == RefundOutcome.RETRYABLE;
  }

  public boolean isManualCompleted() {
    return status == RefundStatus.COMPLETED && completionMethod == RefundCompletionMethod.MANUAL;
  }

  private void complete(
      String providerRefundId,
      UUID completedBy,
      RefundCompletionMethod completionMethod,
      boolean clearFailure,
      boolean clearFailedAt,
      Instant completedAt) {
    Objects.requireNonNull(completionMethod, "completionMethod");
    Objects.requireNonNull(completedAt, "completedAt");
    this.status = RefundStatus.COMPLETED;
    this.outcome = RefundOutcome.COMPLETED;
    if (providerRefundId != null && !providerRefundId.isBlank()) {
      this.providerRefundId = providerRefundId;
    }
    this.completedBy = completedBy;
    this.completionMethod = completionMethod;
    this.completedAt = completedAt;
    if (clearFailedAt) {
      this.failedAt = null;
    }
    if (clearFailure) {
      clearFailure();
    }
  }

  private void recordProviderResult(
      String providerRefundId, String failureCode, String failureMessage, String failureDetails) {
    if (providerRefundId != null && !providerRefundId.isBlank()) {
      this.providerRefundId = providerRefundId;
    }
    if (failureCode != null) {
      this.failureCode = failureCode;
      this.failureMessage = failureMessage;
      this.failureDetails = failureDetails;
      return;
    }
    if (failureMessage != null || failureDetails != null) {
      this.failureMessage = failureMessage;
      this.failureDetails = failureDetails;
    }
  }

  private void clearFailure() {
    this.failureCode = null;
    this.failureMessage = null;
    this.failureDetails = null;
  }
}
