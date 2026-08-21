package io.point3.p3api.gallery.controller.request;

import io.point3.p3api.gallery.domain.type.StoreGalleryItemStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GalleryItemUpdateRequest(
    @Min(0) int sortOrder, boolean featured, @NotNull StoreGalleryItemStatus status) {}
