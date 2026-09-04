package io.point3.p3api.gallery.application.result;

import io.point3.p3api.gallery.domain.entity.StoreGalleryItem;
import io.point3.p3api.gallery.domain.type.StoreGalleryItemStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GalleryItemResult(
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

  public GalleryItemResult {
    variants = List.copyOf(variants);
  }

  public boolean hasDeliveryUrl() {
    return deliveryUrl != null && !deliveryUrl.isBlank();
  }

  public static GalleryItemResult from(
      StoreGalleryItem item, String deliveryUrl, List<Variant> variants) {
    return new GalleryItemResult(
        item.getId(),
        item.getStoreId(),
        item.getAssetId(),
        deliveryUrl,
        item.getSortOrder(),
        item.isFeatured(),
        item.getStatus(),
        item.getCreatedAt(),
        item.getUpdatedAt(),
        variants);
  }

  @Override
  public List<Variant> variants() {
    return List.copyOf(variants);
  }

  public record Variant(String type, String deliveryUrl, int width, int height) {}
}
