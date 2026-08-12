package io.point3.p3api.assetvariant.application.result;

import io.point3.p3api.assetvariant.domain.entity.AssetVariant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public record RegisteredAssetVariants(UUID assetId, List<RegisteredAssetVariant> variants) {

  public static RegisteredAssetVariants from(
      UUID assetId, List<AssetVariant> variants, Function<String, String> deliveryUrlResolver) {
    return new RegisteredAssetVariants(
        assetId,
        variants.stream()
            .map(variant -> RegisteredAssetVariant.from(variant, deliveryUrlResolver))
            .toList());
  }
}
