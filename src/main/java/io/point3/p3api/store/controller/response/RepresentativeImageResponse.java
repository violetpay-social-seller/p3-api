package io.point3.p3api.store.controller.response;

import io.point3.p3api.store.application.representative.result.RepresentativeImageResult;
import io.point3.p3api.store.domain.type.StoreRepresentativeImageStatus;
import java.time.Instant;
import java.util.UUID;

public record RepresentativeImageResponse(
    UUID id,
    UUID storeId,
    UUID assetId,
    int sortOrder,
    StoreRepresentativeImageStatus status,
    Instant createdAt,
    Instant updatedAt) {

  public static RepresentativeImageResponse from(RepresentativeImageResult result) {
    return new RepresentativeImageResponse(
        result.id(),
        result.storeId(),
        result.assetId(),
        result.sortOrder(),
        result.status(),
        result.createdAt(),
        result.updatedAt());
  }
}
