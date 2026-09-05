package io.point3.p3api.store.application.representative.result;

import io.point3.p3api.store.domain.entity.StoreRepresentativeImage;
import io.point3.p3api.store.domain.type.StoreRepresentativeImageStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RepresentativeImageResult(
    UUID id,
    UUID storeId,
    UUID assetId,
    String deliveryUrl,
    int sortOrder,
    StoreRepresentativeImageStatus status,
    Instant createdAt,
    Instant updatedAt,
    List<Variant> variants) {

  public RepresentativeImageResult {
    variants = List.copyOf(variants);
  }

  public static RepresentativeImageResult from(
      StoreRepresentativeImage image, String deliveryUrl, List<Variant> variants) {
    return new RepresentativeImageResult(
        image.getId(),
        image.getStoreId(),
        image.getAssetId(),
        deliveryUrl,
        image.getSortOrder(),
        image.getStatus(),
        image.getCreatedAt(),
        image.getUpdatedAt(),
        variants);
  }

  @Override
  public List<Variant> variants() {
    return List.copyOf(variants);
  }

  public record Variant(String type, String deliveryUrl, int width, int height) {}
}
