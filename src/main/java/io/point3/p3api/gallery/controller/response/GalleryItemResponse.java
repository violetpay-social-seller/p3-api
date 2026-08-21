package io.point3.p3api.gallery.controller.response;

import io.point3.p3api.gallery.application.result.GalleryItemResult;
import io.point3.p3api.gallery.domain.type.StoreGalleryItemStatus;
import java.time.Instant;
import java.util.UUID;

public record GalleryItemResponse(
    UUID id,
    UUID storeId,
    UUID assetId,
    int sortOrder,
    boolean featured,
    StoreGalleryItemStatus status,
    Instant createdAt,
    Instant updatedAt) {

  public static GalleryItemResponse from(GalleryItemResult result) {
    return new GalleryItemResponse(
        result.id(),
        result.storeId(),
        result.assetId(),
        result.sortOrder(),
        result.featured(),
        result.status(),
        result.createdAt(),
        result.updatedAt());
  }
}
