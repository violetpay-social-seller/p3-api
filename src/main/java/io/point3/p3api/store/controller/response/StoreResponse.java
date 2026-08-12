package io.point3.p3api.store.controller.response;

import io.point3.p3api.store.application.result.StoreResult;
import io.point3.p3api.store.domain.type.StoreStatus;
import java.time.Instant;
import java.util.UUID;

public record StoreResponse(
    UUID id,
    UUID ownerUserId,
    UUID profileAssetId,
    UUID bannerAssetId,
    String name,
    String slug,
    String description,
    String contact,
    boolean contactVisible,
    String snsLinks,
    String businessHours,
    String address,
    StoreStatus status,
    Instant createdAt,
    Instant updatedAt) {

  public static StoreResponse from(StoreResult result) {
    return new StoreResponse(
        result.id(),
        result.ownerUserId(),
        result.profileAssetId(),
        result.bannerAssetId(),
        result.name(),
        result.slug(),
        result.description(),
        result.contact(),
        result.contactVisible(),
        result.snsLinks(),
        result.businessHours(),
        result.address(),
        result.status(),
        result.createdAt(),
        result.updatedAt());
  }
}
