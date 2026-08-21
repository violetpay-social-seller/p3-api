package io.point3.p3api.gallery.controller.request;

import io.point3.p3api.gallery.domain.type.StoreGalleryItemStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GalleryItemUpdateRequest(
    @Size(max = 100) String title,
    String description,
    @Min(0) int sortOrder,
    boolean featured,
    @NotNull StoreGalleryItemStatus status) {}
