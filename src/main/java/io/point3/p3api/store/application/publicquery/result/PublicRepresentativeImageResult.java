package io.point3.p3api.store.application.publicquery.result;

import java.util.List;
import java.util.UUID;

public record PublicRepresentativeImageResult(
    UUID id, UUID assetId, String deliveryUrl, int sortOrder, List<Variant> variants) {

  public PublicRepresentativeImageResult {
    variants = List.copyOf(variants);
  }

  @Override
  public List<Variant> variants() {
    return List.copyOf(variants);
  }

  public record Variant(String type, String deliveryUrl, int width, int height) {}
}
