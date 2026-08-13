package io.point3.p3api.store.application.update;

import java.util.UUID;

public record UpdateStoreCommand(
    UUID storeId,
    String name,
    UUID profileAssetId,
    UUID bannerAssetId,
    String description,
    String contact,
    boolean contactVisible,
    String snsLinks,
    String businessHours,
    String address) {}
