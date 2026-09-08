package io.point3.p3api.order.application.result;

import io.point3.p3api.inquiry.domain.type.OrderFormReferenceAssetSource;
import java.util.List;
import java.util.UUID;

public record OrderReferenceAssetResult(
    UUID assetId,
    OrderFormReferenceAssetSource source,
    int sortOrder,
    String status,
    String deliveryUrl,
    List<Variant> variants) {

  public OrderReferenceAssetResult {
    variants = variants == null ? List.of() : List.copyOf(variants);
  }

  @Override
  public List<Variant> variants() {
    return List.copyOf(variants);
  }

  public record Variant(String type, String deliveryUrl, int width, int height) {}
}
