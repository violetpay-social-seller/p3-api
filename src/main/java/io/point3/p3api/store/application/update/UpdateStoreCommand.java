package io.point3.p3api.store.application.update;

import java.util.UUID;

public record UpdateStoreCommand(
    UUID storeId,
    String name,
    UUID profileAssetId,
    String description,
    String contact,
    boolean contactVisible,
    String snsLinks,
    String businessHours,
    String pickupSettings,
    String address) {}
