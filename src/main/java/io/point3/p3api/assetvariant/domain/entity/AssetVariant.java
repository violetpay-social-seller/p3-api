package io.point3.p3api.assetvariant.domain.entity;

import io.point3.p3api.asset.domain.entity.Asset;
import io.point3.p3api.assetvariant.domain.type.AssetVariantStatus;
import io.point3.p3api.assetvariant.domain.type.AssetVariantType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "asset_variants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetVariant {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "asset_id", nullable = false)
  private Asset asset;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 30)
  private AssetVariantType type;

  @Column(name = "object_key", nullable = false, unique = true, length = 1024)
  private String objectKey;

  @Column(name = "content_type", nullable = false, length = 100)
  private String contentType;

  @Column(name = "width", nullable = false)
  private int width;

  @Column(name = "height", nullable = false)
  private int height;

  @Column(name = "size", nullable = false)
  private long size;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private AssetVariantStatus status;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  private AssetVariant(
      Asset asset,
      AssetVariantType type,
      String objectKey,
      String contentType,
      int width,
      int height,
      long size) {
    this.asset = asset;
    this.type = type;
    this.objectKey = objectKey;
    this.contentType = contentType;
    this.width = width;
    this.height = height;
    this.size = size;
    this.status = AssetVariantStatus.READY;
  }

  public static AssetVariant create(
      Asset asset,
      AssetVariantType type,
      String objectKey,
      String contentType,
      int width,
      int height,
      long size) {
    Objects.requireNonNull(asset, "asset");
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(objectKey, "objectKey");
    Objects.requireNonNull(contentType, "contentType");

    if (width <= 0) {
      throw new IllegalArgumentException("width must be greater than 0");
    }

    if (height <= 0) {
      throw new IllegalArgumentException("height must be greater than 0");
    }

    if (size < 0) {
      throw new IllegalArgumentException("size must be greater than or equal to 0");
    }

    return new AssetVariant(asset, type, objectKey, contentType, width, height, size);
  }

  public void markFailed() {
    this.status = AssetVariantStatus.FAILED;
  }

  public void delete() {
    this.status = AssetVariantStatus.DELETED;
  }
}
