package io.point3.p3api.operator.application.result;

import io.point3.p3api.gallery.domain.entity.StoreGalleryItem;
import io.point3.p3api.gallery.domain.type.StoreGalleryItemStatus;
import java.time.Instant;
import java.util.UUID;

public record OperatorGalleryItemResult(
    UUID id,
    UUID storeId,
    UUID assetId,
    int sortOrder,
    boolean featured,
    StoreGalleryItemStatus status,
    Instant createdAt,
    Instant updatedAt) {

  public static OperatorGalleryItemResult from(StoreGalleryItem item) {
    return new OperatorGalleryItemResult(
        item.getId(),
        item.getStoreId(),
        item.getAssetId(),
        item.getSortOrder(),
        item.isFeatured(),
        item.getStatus(),
        item.getCreatedAt(),
        item.getUpdatedAt());
  }
}
