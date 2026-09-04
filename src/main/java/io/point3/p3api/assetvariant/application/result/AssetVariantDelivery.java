package io.point3.p3api.assetvariant.application.result;

import java.util.List;

public record AssetVariantDelivery(String deliveryUrl, List<Variant> variants) {

  public AssetVariantDelivery {
    variants = List.copyOf(variants);
  }

  public static AssetVariantDelivery empty() {
    return new AssetVariantDelivery(null, List.of());
  }

  public boolean isReady() {
    return deliveryUrl != null && !deliveryUrl.isBlank();
  }

  @Override
  public List<Variant> variants() {
    return List.copyOf(variants);
  }

  public record Variant(String type, String deliveryUrl, int width, int height) {}
}
