package io.point3.p3api.gallery.application.command;

import io.point3.p3api.gallery.domain.type.StoreGalleryItemStatus;
import java.util.UUID;

public record UpdateGalleryItemCommand(
    UUID storeId,
    UUID galleryItemId,
    int sortOrder,
    boolean featured,
    StoreGalleryItemStatus status) {}
