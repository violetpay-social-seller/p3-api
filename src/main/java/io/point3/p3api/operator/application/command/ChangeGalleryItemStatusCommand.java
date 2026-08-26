package io.point3.p3api.operator.application.command;

import io.point3.p3api.gallery.domain.type.StoreGalleryItemStatus;
import java.util.UUID;

public record ChangeGalleryItemStatusCommand(
    UUID galleryItemId, UUID operatorUserId, StoreGalleryItemStatus status, String reason) {

  public static ChangeGalleryItemStatusCommand of(
      UUID galleryItemId, UUID operatorUserId, StoreGalleryItemStatus status, String reason) {
    return new ChangeGalleryItemStatusCommand(galleryItemId, operatorUserId, status, reason);
  }
}
