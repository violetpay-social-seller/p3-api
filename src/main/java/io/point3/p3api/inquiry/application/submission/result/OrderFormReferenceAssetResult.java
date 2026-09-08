package io.point3.p3api.inquiry.application.submission.result;

import io.point3.p3api.inquiry.domain.type.OrderFormReferenceAssetSource;
import java.util.List;
import java.util.UUID;

public record OrderFormReferenceAssetResult(
    UUID assetId,
    OrderFormReferenceAssetSource source,
    int sortOrder,
    String status,
    String deliveryUrl,
    List<Variant> variants) {

  public OrderFormReferenceAssetResult {
    variants = variants == null ? List.of() : List.copyOf(variants);
  }

  public static OrderFormReferenceAssetResult of(
      UUID assetId,
      OrderFormReferenceAssetSource source,
      int sortOrder,
      OrderFormAssetDelivery delivery) {
    return new OrderFormReferenceAssetResult(
        assetId,
        source,
        sortOrder,
        delivery.status(),
        delivery.deliveryUrl(),
        delivery.variants().stream().map(Variant::from).toList());
  }

  @Override
  public List<Variant> variants() {
    return List.copyOf(variants);
  }

  public record Variant(String type, String deliveryUrl, int width, int height) {
    private static Variant from(OrderFormAssetDelivery.Variant variant) {
      return new Variant(variant.type(), variant.deliveryUrl(), variant.width(), variant.height());
    }
  }
}
