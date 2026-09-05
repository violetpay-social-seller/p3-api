package io.point3.p3api.store.controller.response;

import io.point3.p3api.store.application.representative.result.RepresentativeImageResult;
import io.point3.p3api.store.domain.type.StoreRepresentativeImageStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RepresentativeImageResponse(
    UUID id,
    UUID storeId,
    UUID assetId,
    String deliveryUrl,
    int sortOrder,
    StoreRepresentativeImageStatus status,
    Instant createdAt,
    Instant updatedAt,
    List<Variant> variants) {

  public RepresentativeImageResponse {
    variants = List.copyOf(variants);
  }

  public static RepresentativeImageResponse from(RepresentativeImageResult result) {
    return new RepresentativeImageResponse(
        result.id(),
        result.storeId(),
        result.assetId(),
        result.deliveryUrl(),
        result.sortOrder(),
        result.status(),
        result.createdAt(),
        result.updatedAt(),
        result.variants().stream().map(Variant::from).toList());
  }

  @Override
  public List<Variant> variants() {
    return List.copyOf(variants);
  }

  public record Variant(String type, String deliveryUrl, int width, int height) {

    public static Variant from(RepresentativeImageResult.Variant variant) {
      return new Variant(variant.type(), variant.deliveryUrl(), variant.width(), variant.height());
    }
  }
}
