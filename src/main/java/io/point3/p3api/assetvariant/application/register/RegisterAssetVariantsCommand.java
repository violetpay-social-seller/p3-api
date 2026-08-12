package io.point3.p3api.assetvariant.application.register;

import io.point3.p3api.assetvariant.controller.AssetVariantRequest;
import io.point3.p3api.assetvariant.domain.type.AssetVariantType;
import java.util.List;
import java.util.UUID;

public record RegisterAssetVariantsCommand(UUID assetId, List<Variant> variants) {

  public static RegisterAssetVariantsCommand from(UUID assetId, AssetVariantRequest request) {
    return new RegisterAssetVariantsCommand(
        assetId, request.variants().stream().map(Variant::from).toList());
  }

  public record Variant(
      AssetVariantType type,
      String objectKey,
      String contentType,
      int width,
      int height,
      long sizeBytes) {

    public static Variant from(AssetVariantRequest.Variant variant) {
      return new Variant(
          variant.type(),
          variant.objectKey(),
          variant.contentType(),
          variant.width(),
          variant.height(),
          variant.sizeBytes());
    }
  }
}
