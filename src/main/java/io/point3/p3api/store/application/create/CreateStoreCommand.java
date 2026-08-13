package io.point3.p3api.store.application.create;

import java.util.UUID;

public record CreateStoreCommand(
    UUID ownerUserId,
    String name,
    UUID profileAssetId,
    UUID bannerAssetId,
    String description,
    String contact,
    boolean contactVisible,
    String snsLinks,
    String businessHours,
    String address) {}
