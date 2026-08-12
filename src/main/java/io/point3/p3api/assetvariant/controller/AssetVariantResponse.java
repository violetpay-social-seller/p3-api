package io.point3.p3api.assetvariant.controller;

import io.point3.p3api.assetvariant.application.result.RegisteredAssetVariant;
import io.point3.p3api.assetvariant.application.result.RegisteredAssetVariants;
import io.point3.p3api.assetvariant.domain.type.AssetVariantType;
import java.util.List;
import java.util.UUID;

public record AssetVariantResponse(UUID assetId, List<Variant> variants) {

  public static AssetVariantResponse from(RegisteredAssetVariants result) {
    return new AssetVariantResponse(
        result.assetId(), result.variants().stream().map(Variant::from).toList());
  }

  public record Variant(UUID variantId, AssetVariantType type, String deliveryUrl) {

    public static Variant from(RegisteredAssetVariant variant) {
      return new Variant(variant.variantId(), variant.type(), variant.deliveryUrl());
    }
  }
}
