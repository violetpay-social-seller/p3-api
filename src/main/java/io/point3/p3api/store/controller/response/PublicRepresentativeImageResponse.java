package io.point3.p3api.store.controller.response;

import io.point3.p3api.store.application.publicquery.result.PublicRepresentativeImageResult;
import java.util.UUID;

public record PublicRepresentativeImageResponse(
    UUID id, UUID assetId, String deliveryUrl, int sortOrder) {

  public static PublicRepresentativeImageResponse from(PublicRepresentativeImageResult result) {
    return new PublicRepresentativeImageResponse(
        result.id(), result.assetId(), result.deliveryUrl(), result.sortOrder());
  }
}
