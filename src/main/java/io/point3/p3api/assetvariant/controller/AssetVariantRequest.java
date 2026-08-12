package io.point3.p3api.assetvariant.controller;

import io.point3.p3api.assetvariant.domain.type.AssetVariantType;
import java.util.List;

public record AssetVariantRequest(List<Variant> variants) {

  public AssetVariantRequest {
    variants = List.copyOf(variants);
  }

  @Override
  public List<Variant> variants() {
    return List.copyOf(variants);
  }

  public record Variant(
      AssetVariantType type,
      String objectKey,
      String contentType,
      int width,
      int height,
      long sizeBytes) {}
}
