package io.point3.p3api.gallery.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record GalleryItemCreateRequest(
    @NotNull UUID assetId,
    @Size(max = 100) String title,
    String description,
    @Min(0) int sortOrder,
    boolean featured) {}
