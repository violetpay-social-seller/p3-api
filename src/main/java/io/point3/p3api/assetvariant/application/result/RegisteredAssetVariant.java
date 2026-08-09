package io.point3.p3api.assetvariant.application.result;

import io.point3.p3api.assetvariant.domain.entity.AssetVariant;
import io.point3.p3api.assetvariant.domain.type.AssetVariantType;
import java.util.UUID;
import java.util.function.Function;

public record RegisteredAssetVariant(UUID variantId, AssetVariantType type, String deliveryUrl) {

  public static RegisteredAssetVariant from(
      AssetVariant variant, Function<String, String> deliveryUrlResolver) {
    return new RegisteredAssetVariant(
        variant.getId(), variant.getType(), deliveryUrlResolver.apply(variant.getObjectKey()));
  }
}
