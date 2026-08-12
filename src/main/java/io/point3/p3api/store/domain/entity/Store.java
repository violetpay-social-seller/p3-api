package io.point3.p3api.store.domain.entity;

import io.point3.p3api.store.domain.type.StoreStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "stores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "owner_user_id", nullable = false, unique = true)
  private UUID ownerUserId;

  @Column(name = "profile_asset_id")
  private UUID profileAssetId;

  @Column(name = "banner_asset_id")
  private UUID bannerAssetId;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "slug", nullable = false, unique = true, length = 120)
  private String slug;

  @Column(name = "description", columnDefinition = "text")
  private String description;

  @Column(name = "contact", length = 100)
  private String contact;

  @Column(name = "contact_visible", nullable = false)
  private boolean contactVisible;

  @Column(name = "sns_links", columnDefinition = "jsonb")
  private String snsLinks;

  @Column(name = "business_hours", columnDefinition = "jsonb")
  private String businessHours;

  @Column(name = "address", length = 255)
  private String address;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private StoreStatus status;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  private Store(UUID ownerUserId, String name, String slug) {
    this.ownerUserId = ownerUserId;
    this.name = name;
    this.slug = slug;
    this.contactVisible = false;
    this.status = StoreStatus.ACTIVE;
  }

  public static Store create(UUID ownerUserId, String name, String slug) {
    Objects.requireNonNull(ownerUserId, "ownerUserId");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(slug, "slug");

    return new Store(ownerUserId, name, slug);
  }

  public void updateProfileAsset(UUID profileAssetId) {
    this.profileAssetId = profileAssetId;
  }

  public void updateBannerAsset(UUID bannerAssetId) {
    this.bannerAssetId = bannerAssetId;
  }

  public boolean isActive() {
    return this.status == StoreStatus.ACTIVE;
  }

  public void inactive() {
    ensureActive("Only active store can inactive");
    this.status = StoreStatus.INACTIVE;
  }

  public void suspend() {
    ensureActive("Only active store can inactive");
    this.status = StoreStatus.SUSPENDED;
  }

  public void delete() {
    this.status = StoreStatus.DELETED;
  }

  private void ensureActive(String message) {
    if (!isActive()) {
      throw new IllegalArgumentException(message); // TODO:Store 도메인 예외로 변경 필요
    }
  }
}
