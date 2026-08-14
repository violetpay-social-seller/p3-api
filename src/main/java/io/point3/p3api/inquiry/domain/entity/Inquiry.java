package io.point3.p3api.inquiry.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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

  @Column(name = "buyer_user_id", nullable = false)
  private UUID buyerUserId;

  @Column(name = "context_product_id")
  private UUID contextProductId;

  @Column(name = "buyer_last_read_at")
  private Instant buyerLastReadAt;

  @Column(name = "seller_last_read_at")
  private Instant sellerLastReadAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  public void changeContextProduct(UUID productId) {
    this.contextProductId = Objects.requireNonNull(productId, "productId");
  }

  public void clearContextProduct() {
    this.contextProductId = null;
  }

  private Inquiry(UUID storeId, UUID buyerUserId, UUID contextProductId) {
    this.storeId = storeId;
    this.buyerUserId = buyerUserId;
    this.contextProductId = contextProductId;
  }

  public static Inquiry create(UUID storeId, UUID buyerUserId, UUID contextProductId) {
    Objects.requireNonNull(storeId, "storeId");
    Objects.requireNonNull(buyerUserId, "buyerUserId");

    return new Inquiry(storeId, buyerUserId, contextProductId);
  }
}
