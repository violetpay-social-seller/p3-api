package io.point3.p3api.gallery.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record GalleryItemCreateRequest(
    @NotNull UUID assetId, @Min(0) int sortOrder, boolean featured) {}
