package io.point3.p3api.store.controller.response;

import io.point3.p3api.store.application.publicquery.result.PublicStoreResult;
import java.util.List;
import java.util.UUID;

public record PublicStoreResponse(
    UUID id,
    UUID profileAssetId,
    String profileDeliveryUrl,
    String name,
    String slug,
    String description,
    String contact,
    boolean contactVisible,
    String snsLinks,
    String businessHours,
    String address,
    List<PublicRepresentativeImageResponse> representativeImages) {

  public PublicStoreResponse {
    representativeImages = List.copyOf(representativeImages);
  }

  public static PublicStoreResponse from(PublicStoreResult result) {
    return new PublicStoreResponse(
        result.id(),
        result.profileAssetId(),
        result.profileDeliveryUrl(),
        result.name(),
        result.slug(),
        result.description(),
        result.contact(),
        result.contactVisible(),
        result.snsLinks(),
        result.businessHours(),
        result.address(),
        result.representativeImages().stream()
            .map(PublicRepresentativeImageResponse::from)
            .toList());
  }

  @Override
  public List<PublicRepresentativeImageResponse> representativeImages() {
    return List.copyOf(representativeImages);
  }
}
