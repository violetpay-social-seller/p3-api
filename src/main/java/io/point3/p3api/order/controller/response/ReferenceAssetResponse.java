package io.point3.p3api.order.controller.response;

import io.point3.p3api.inquiry.application.submission.result.OrderFormReferenceAssetResult;
import io.point3.p3api.inquiry.domain.type.OrderFormReferenceAssetSource;
import io.point3.p3api.order.application.result.OrderReferenceAssetResult;
import java.util.List;
import java.util.UUID;

public record ReferenceAssetResponse(
    UUID assetId,
    OrderFormReferenceAssetSource source,
    int sortOrder,
    String status,
    String deliveryUrl,
    List<VariantResponse> variants) {

  public ReferenceAssetResponse {
    variants = List.copyOf(variants);
  }

  public static ReferenceAssetResponse from(OrderFormReferenceAssetResult result) {
    return new ReferenceAssetResponse(
        result.assetId(),
        result.source(),
        result.sortOrder(),
        result.status(),
        result.deliveryUrl(),
        result.variants().stream().map(VariantResponse::from).toList());
  }

  public static ReferenceAssetResponse from(OrderReferenceAssetResult result) {
    return new ReferenceAssetResponse(
        result.assetId(),
        result.source(),
        result.sortOrder(),
        result.status(),
        result.deliveryUrl(),
        result.variants().stream().map(VariantResponse::from).toList());
  }

  @Override
  public List<VariantResponse> variants() {
    return List.copyOf(variants);
  }

  public record VariantResponse(String type, String deliveryUrl, int width, int height) {
    private static VariantResponse from(OrderFormReferenceAssetResult.Variant variant) {
      return new VariantResponse(
          variant.type(), variant.deliveryUrl(), variant.width(), variant.height());
    }

    private static VariantResponse from(OrderReferenceAssetResult.Variant variant) {
      return new VariantResponse(
          variant.type(), variant.deliveryUrl(), variant.width(), variant.height());
    }
  }
}
