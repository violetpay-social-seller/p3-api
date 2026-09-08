package io.point3.p3api.inquiry.application.submission.result;

import io.point3.p3api.assetvariant.application.result.AssetVariantDelivery;
import java.util.List;

public record OrderFormAssetDelivery(String status, String deliveryUrl, List<Variant> variants) {

  public OrderFormAssetDelivery {
    variants = variants == null ? List.of() : List.copyOf(variants);
  }

  public static OrderFormAssetDelivery missing() {
    return new OrderFormAssetDelivery("MISSING", null, List.of());
  }

  public static OrderFormAssetDelivery from(
      String status, String originalDeliveryUrl, AssetVariantDelivery variantDelivery) {
    String deliveryUrl =
        variantDelivery.deliveryUrl() == null ? originalDeliveryUrl : variantDelivery.deliveryUrl();
    return new OrderFormAssetDelivery(
        status,
        deliveryUrl,
        variantDelivery.variants().stream().map(Variant::from).toList());
  }

  @Override
  public List<Variant> variants() {
    return List.copyOf(variants);
  }

  public record Variant(String type, String deliveryUrl, int width, int height) {
    private static Variant from(AssetVariantDelivery.Variant variant) {
      return new Variant(variant.type(), variant.deliveryUrl(), variant.width(), variant.height());
    }
  }
}
