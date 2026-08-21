package io.point3.p3api.store.application.representative.result;

import io.point3.p3api.store.domain.entity.StoreRepresentativeImage;
import io.point3.p3api.store.domain.type.StoreRepresentativeImageStatus;
import java.time.Instant;
import java.util.UUID;

public record RepresentativeImageResult(
    UUID id,
    UUID storeId,
    UUID assetId,
    int sortOrder,
    StoreRepresentativeImageStatus status,
    Instant createdAt,
    Instant updatedAt) {

  public static RepresentativeImageResult from(StoreRepresentativeImage image) {
    return new RepresentativeImageResult(
        image.getId(),
        image.getStoreId(),
        image.getAssetId(),
        image.getSortOrder(),
        image.getStatus(),
        image.getCreatedAt(),
        image.getUpdatedAt());
  }
}
