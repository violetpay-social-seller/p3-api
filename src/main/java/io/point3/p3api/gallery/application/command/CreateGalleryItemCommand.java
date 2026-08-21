package io.point3.p3api.gallery.application.command;

import java.util.UUID;

public record CreateGalleryItemCommand(
    UUID storeId,
    UUID assetId,
    String title,
    String description,
    int sortOrder,
    boolean featured) {}
