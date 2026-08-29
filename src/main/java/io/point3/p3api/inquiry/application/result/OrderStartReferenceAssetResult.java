package io.point3.p3api.inquiry.application.result;

import io.point3.p3api.inquiry.domain.entity.OrderStartReferenceAsset;
import io.point3.p3api.inquiry.domain.type.OrderFormReferenceAssetSource;
import java.util.UUID;

public record OrderStartReferenceAssetResult(
    UUID assetId, OrderFormReferenceAssetSource source, int sortOrder) {

  public static OrderStartReferenceAssetResult from(OrderStartReferenceAsset asset) {
    return new OrderStartReferenceAssetResult(
        asset.getAssetId(), asset.getSource(), asset.getSortOrder());
  }
}
