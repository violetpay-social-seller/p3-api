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

  @Column(name = "pickup_settings", columnDefinition = "jsonb")
  private String pickupSettings;

  @Column(name = "order_notice", columnDefinition = "text")
  private String orderNotice;

  @Column(name = "cancellation_refund_policy", columnDefinition = "text")
  private String cancellationRefundPolicy;

  @Column(name = "address", length = 255)
  private String address;

  @Column(name = "settlement_account_status", nullable = false, length = 30)
  private String settlementAccountStatus;

  @Column(name = "settlement_account_registered_at")
  private Instant settlementAccountRegisteredAt;

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
    this.settlementAccountStatus = "NOT_REGISTERED";
    this.status = StoreStatus.INACTIVE;
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

  public void updateBasicInfo(
      String name,
      String description,
      String contact,
      boolean contactVisible,
      String snsLinks,
      String businessHours,
      String address) {
    Objects.requireNonNull(name, "name");

    this.name = name;
    this.description = description;
    this.contact = contact;
    this.contactVisible = contactVisible;
    this.snsLinks = snsLinks;
    this.businessHours = businessHours;
    this.address = address;
  }

  public void updatePickupSettings(String pickupSettings) {
    this.pickupSettings = pickupSettings;
  }

  public void markSettlementAccountInputCompleted(Instant registeredAt) {
    Objects.requireNonNull(registeredAt, "registeredAt");
    this.settlementAccountStatus = "INPUT_COMPLETED";
    this.settlementAccountRegisteredAt = registeredAt;
  }

  public boolean isActive() {
    return this.status == StoreStatus.ACTIVE;
  }

  public void active() {
    if (this.status == StoreStatus.DELETED) {
      throw new IllegalArgumentException("Deleted store can not active");
    }
    this.status = StoreStatus.ACTIVE;
  }

  public void inactive() {
    if (this.status == StoreStatus.DELETED) {
      throw new IllegalArgumentException("Deleted store can not inactive");
    }
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
