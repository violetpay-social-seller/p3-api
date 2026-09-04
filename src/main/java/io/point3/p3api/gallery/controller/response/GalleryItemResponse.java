package io.point3.p3api.gallery.controller.response;

import io.point3.p3api.gallery.application.result.GalleryItemResult;
import io.point3.p3api.gallery.domain.type.StoreGalleryItemStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GalleryItemResponse(
    UUID id,
    UUID storeId,
    UUID assetId,
    String deliveryUrl,
    int sortOrder,
    boolean featured,
    StoreGalleryItemStatus status,
    Instant createdAt,
    Instant updatedAt,
    List<Variant> variants) {

  public GalleryItemResponse {
    variants = List.copyOf(variants);
  }

  public static GalleryItemResponse from(GalleryItemResult result) {
    return new GalleryItemResponse(
        result.id(),
        result.storeId(),
        result.assetId(),
        result.deliveryUrl(),
        result.sortOrder(),
        result.featured(),
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

    public static Variant from(GalleryItemResult.Variant variant) {
      return new Variant(variant.type(), variant.deliveryUrl(), variant.width(), variant.height());
    }
  }
}
