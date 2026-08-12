package io.point3.p3api.store.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record StoreUpdateRequest(
    @NotBlank @Size(max = 100) String name,
    UUID profileAssetId,
    UUID bannerAssetId,
    String description,
    @Size(max = 100) String contact,
    boolean contactVisible,
    String snsLinks,
    String businessHours,
    @Size(max = 255) String address) {}
