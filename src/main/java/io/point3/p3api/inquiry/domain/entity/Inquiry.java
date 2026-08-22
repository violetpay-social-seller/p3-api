package io.point3.p3api.inquiry.domain.entity;

import io.point3.p3api.inquiry.domain.type.InquiryStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "inquiries",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_inquiries_store_buyer",
            columnNames = {"store_id", "buyer_user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inquiry {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "store_id", nullable = false)
  private UUID storeId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private InquiryStatus status;

  @Column(name = "buyer_user_id", nullable = false)
  private UUID buyerUserId;

  @Column(name = "buyer_last_read_at")
  private Instant buyerLastReadAt;

  @Column(name = "seller_last_read_at")
  private Instant sellerLastReadAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  private Inquiry(UUID storeId, UUID buyerUserId) {
    this.storeId = storeId;
    this.buyerUserId = buyerUserId;
    this.status = InquiryStatus.WAITING;
  }

  public static Inquiry create(UUID storeId, UUID buyerUserId) {
    Objects.requireNonNull(storeId, "storeId");
    Objects.requireNonNull(buyerUserId, "buyerUserId");

    return new Inquiry(storeId, buyerUserId);
  }

  public void markWaiting() {
    this.status = InquiryStatus.WAITING;
  }

  public void markInProgress() {
    this.status = InquiryStatus.IN_PROGRESS;
  }

  public void markPaid() {
    this.status = InquiryStatus.PAID;
  }

  public void markPickedUp() {
    this.status = InquiryStatus.PICKED_UP;
  }

  public void markBuyerRead(Instant readAt) {
    this.buyerLastReadAt = Objects.requireNonNull(readAt, "readAt");
  }

  public void markSellerRead(Instant readAt) {
    this.sellerLastReadAt = Objects.requireNonNull(readAt, "readAt");
  }
}
