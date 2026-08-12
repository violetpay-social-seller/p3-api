package io.point3.p3api.inquiry.domain.entity;

import io.point3.p3api.inquiry.domain.type.InquiryStatus;
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
@Table(name = "inquiries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inquiry {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "store_id", nullable = false)
  private UUID storeId;

  @Column(name = "buyer_user_id", nullable = false)
  private UUID buyerUserId;

  @Column(name = "context_product_id")
  private UUID contextProductId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private InquiryStatus status;

  @Column(name = "buyer_last_read_at")
  private Instant buyerLastReadAt;

  @Column(name = "seller_last_read_at")
  private Instant sellerLastReadAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "closed_at")
  private Instant closedAt;

  private Inquiry(UUID storeId, UUID buyerUserId, UUID contextProductId) {
    this.storeId = storeId;
    this.buyerUserId = buyerUserId;
    this.contextProductId = contextProductId;
    this.status = InquiryStatus.OPEN;
  }

  public static Inquiry create(UUID storeId, UUID buyerUserId, UUID contextProductId) {
    Objects.requireNonNull(storeId, "storeId");
    Objects.requireNonNull(buyerUserId, "buyerUserId");

    return new Inquiry(storeId, buyerUserId, contextProductId);
  }

  public void close(Instant closedAt) {
    Objects.requireNonNull(closedAt, "closedAt");
    this.status = InquiryStatus.CLOSED;
    this.closedAt = closedAt;
  }
}
