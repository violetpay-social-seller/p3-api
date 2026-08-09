package io.point3.p3api.asset.application.result;

import io.point3.p3api.asset.domain.entity.Asset;
import java.util.UUID;

public record RegistryAsset(UUID assetId, String deliveryUrl) {
  public static RegistryAsset from(Asset registeredAsset, String deliveryUrl) {
    return new RegistryAsset(registeredAsset.getId(), deliveryUrl);
  }
}
