package io.point3.p3api.store.domain.entity;

import io.point3.p3api.store.domain.type.StoreRepresentativeImageStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    name = "store_representative_images",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_store_representative_images_store_sort_order",
            columnNames = {"store_id", "sort_order"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreRepresentativeImage {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "store_id", nullable = false)
  private UUID storeId;

  @Column(name = "asset_id", nullable = false)
  private UUID assetId;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private StoreRepresentativeImageStatus status;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  private StoreRepresentativeImage(UUID storeId, UUID assetId, int sortOrder) {
    this.storeId = storeId;
    this.assetId = assetId;
    this.sortOrder = sortOrder;
    this.status = StoreRepresentativeImageStatus.ACTIVE;
  }

  public static StoreRepresentativeImage create(UUID storeId, UUID assetId, int sortOrder) {
    Objects.requireNonNull(storeId, "storeId");
    Objects.requireNonNull(assetId, "assetId");

    if (sortOrder < 0) {
      throw new IllegalArgumentException("sortOrder must be greater than or equal to 0");
    }

    return new StoreRepresentativeImage(storeId, assetId, sortOrder);
  }

  public void changeSortOrder(int sortOrder) {
    if (sortOrder < 0) {
      throw new IllegalArgumentException("sortOrder must be greater than or equal to 0");
    }
    this.sortOrder = sortOrder;
  }

  public void show() {
    this.status = StoreRepresentativeImageStatus.ACTIVE;
  }

  public void hide() {
    this.status = StoreRepresentativeImageStatus.HIDDEN;
  }
}
