package io.point3.p3api.store.domain.entity;

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
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "store_refund_policies",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_store_refund_policies_store_sort_order",
          columnNames = {"store_id", "sort_order"}),
      @UniqueConstraint(
          name = "uk_store_refund_policies_store_days_before",
          columnNames = {"store_id", "days_before_pickup"})
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreRefundPolicy {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "store_id", nullable = false)
  private UUID storeId;

  @Column(name = "days_before_pickup", nullable = false)
  private int daysBeforePickup;

  @Column(name = "refund_rate", nullable = false)
  private int refundRate;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  private StoreRefundPolicy(UUID storeId, int daysBeforePickup, int refundRate, int sortOrder) {
    this.storeId = storeId;
    this.daysBeforePickup = daysBeforePickup;
    this.refundRate = refundRate;
    this.sortOrder = sortOrder;
  }

  public static StoreRefundPolicy create(
      UUID storeId, int daysBeforePickup, int refundRate, int sortOrder) {
    Objects.requireNonNull(storeId, "storeId");
    return new StoreRefundPolicy(storeId, daysBeforePickup, refundRate, sortOrder);
  }
}
