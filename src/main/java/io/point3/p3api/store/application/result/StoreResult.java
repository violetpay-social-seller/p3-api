package io.point3.p3api.store.application.result;

import io.point3.p3api.store.domain.entity.Store;
import io.point3.p3api.store.domain.type.StoreStatus;
import java.time.Instant;
import java.util.UUID;

public record StoreResult(
    UUID id,
    UUID ownerUserId,
    UUID profileAssetId,
    String name,
    String slug,
    String description,
    String contact,
    boolean contactVisible,
    String snsLinks,
    String businessHours,
    String pickupSettings,
    String address,
    String settlementAccountStatus,
    Instant settlementAccountRegisteredAt,
    StoreStatus status,
    Instant createdAt,
    Instant updatedAt) {

  public static StoreResult from(Store store) {
    return new StoreResult(
        store.getId(),
        store.getOwnerUserId(),
        store.getProfileAssetId(),
        store.getName(),
        store.getSlug(),
        store.getDescription(),
        store.getContact(),
        store.isContactVisible(),
        store.getSnsLinks(),
        store.getBusinessHours(),
        store.getPickupSettings(),
        store.getAddress(),
        store.getSettlementAccountStatus(),
        store.getSettlementAccountRegisteredAt(),
        store.getStatus(),
        store.getCreatedAt(),
        store.getUpdatedAt());
  }
}
