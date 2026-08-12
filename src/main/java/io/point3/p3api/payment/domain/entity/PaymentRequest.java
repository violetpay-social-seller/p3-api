package io.point3.p3api.payment.domain.entity;

import io.point3.p3api.payment.domain.type.PaymentRequestStatus;
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
@Table(name = "payment_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "inquiry_id", nullable = false)
  private UUID inquiryId;

  @Column(name = "confirmation_id", nullable = false)
  private UUID confirmationId;

  @Column(name = "requested_by", nullable = false)
  private UUID requestedBy;

  @Column(name = "amount", nullable = false)
  private long amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private PaymentRequestStatus status;

  @CreationTimestamp
  @Column(name = "requested_at", nullable = false, updatable = false)
  private Instant requestedAt;

  @Column(name = "expires_at")
  private Instant expiresAt;

  private PaymentRequest(
      UUID inquiryId, UUID confirmationId, UUID requestedBy, long amount, Instant expiresAt) {
    this.inquiryId = inquiryId;
    this.confirmationId = confirmationId;
    this.requestedBy = requestedBy;
    this.amount = amount;
    this.expiresAt = expiresAt;
    this.status = PaymentRequestStatus.PENDING;
  }

  public static PaymentRequest create(
      UUID inquiryId, UUID confirmationId, UUID requestedBy, long amount, Instant expiresAt) {
    Objects.requireNonNull(inquiryId, "inquiryId");
    Objects.requireNonNull(confirmationId, "confirmationId");
    Objects.requireNonNull(requestedBy, "requestedBy");

    if (amount < 0) {
      throw new IllegalArgumentException("amount must be greater than or equal to 0");
    }

    return new PaymentRequest(inquiryId, confirmationId, requestedBy, amount, expiresAt);
  }
}
