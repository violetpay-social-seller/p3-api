package io.point3.p3api.payment.domain.entity;

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

  @Column(name = "reason", columnDefinition = "text")
  private String reason;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private RefundStatus status;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "completed_at")
  private Instant completedAt;

  private Refund(
      UUID orderId, UUID paymentAttemptId, UUID requestedBy, long amount, String reason) {
    this.orderId = orderId;
    this.paymentAttemptId = paymentAttemptId;
    this.requestedBy = requestedBy;
    this.amount = amount;
    this.reason = reason;
    this.status = RefundStatus.REQUESTED;
  }

  public static Refund create(
      UUID orderId, UUID paymentAttemptId, UUID requestedBy, long amount, String reason) {
    Objects.requireNonNull(orderId, "orderId");
    Objects.requireNonNull(paymentAttemptId, "paymentAttemptId");
    Objects.requireNonNull(requestedBy, "requestedBy");

    if (amount < 0) {
      throw new IllegalArgumentException("amount must be greater than or equal to 0");
    }

    return new Refund(orderId, paymentAttemptId, requestedBy, amount, reason);
  }

  public void complete(Instant completedAt) {
    Objects.requireNonNull(completedAt, "completedAt");
    this.status = RefundStatus.COMPLETED;
    this.completedAt = completedAt;
  }

  public void fail() {
    this.status = RefundStatus.FAILED;
  }
}
