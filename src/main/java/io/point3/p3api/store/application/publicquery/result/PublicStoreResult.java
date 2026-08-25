package io.point3.p3api.store.application.publicquery.result;

import java.util.List;
import java.util.UUID;

public record PublicStoreResult(
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
    List<PublicRepresentativeImageResult> representativeImages) {

  public PublicStoreResult {
    representativeImages = List.copyOf(representativeImages);
  }

  @Override
  public List<PublicRepresentativeImageResult> representativeImages() {
    return List.copyOf(representativeImages);
  }
}
