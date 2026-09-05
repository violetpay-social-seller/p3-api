package io.point3.p3api.inquiry.application.result;

import io.point3.p3api.inquiry.domain.entity.OrderStartReferenceAsset;
import io.point3.p3api.inquiry.domain.type.OrderFormReferenceAssetSource;
import java.util.UUID;

public record OrderStartReferenceAssetResult(
    UUID assetId, OrderFormReferenceAssetSource source, String deliveryUrl) {

  public static OrderStartReferenceAssetResult from(
      OrderStartReferenceAsset asset, String deliveryUrl) {
    return new OrderStartReferenceAssetResult(asset.getAssetId(), asset.getSource(), deliveryUrl);
  }
}
