package io.point3.p3api.store.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record StoreCreateRequest(
    @NotBlank @Size(max = 100) String name,
    UUID profileAssetId,
    String description,
    @Size(max = 100) String contact,
    boolean contactVisible,
    String snsLinks,
    String businessHours,
    String pickupSettings,
    @Size(max = 255) String address) {}
