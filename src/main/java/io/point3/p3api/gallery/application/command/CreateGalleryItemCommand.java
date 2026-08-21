package io.point3.p3api.gallery.application.command;

import java.util.UUID;

public record CreateGalleryItemCommand(
    UUID storeId, UUID assetId, int sortOrder, boolean featured) {}
