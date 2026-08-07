package io.point3.p3api.asset;

import io.point3.p3api.asset.application.result.RegistryAsset;
import java.util.UUID;

public record AssetResponse(UUID assetId, String deliveryUrl) {

    public static AssetResponse from(RegistryAsset asset) {
        return new AssetResponse(asset.assetId(), asset.deliveryUrl());
    }
}
