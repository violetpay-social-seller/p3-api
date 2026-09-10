package io.point3.p3api.store.controller.response;

import io.point3.p3api.store.application.profileimage.SellerProfileImageResult;
import java.util.UUID;

public record SellerProfileImageResponse(UUID profileAssetId, String profileImageDeliveryUrl) {

  public static SellerProfileImageResponse from(SellerProfileImageResult result) {
    return new SellerProfileImageResponse(
        result.profileAssetId(), result.profileImageDeliveryUrl());
  }
}
