package io.point3.p3api.store.controller.response;

import io.point3.p3api.store.application.publicquery.result.PublicRepresentativeImageResult;
import java.util.List;
import java.util.UUID;

public record PublicRepresentativeImageResponse(
    UUID id, UUID assetId, String deliveryUrl, int sortOrder, List<Variant> variants) {

  public PublicRepresentativeImageResponse {
    variants = List.copyOf(variants);
  }

  public static PublicRepresentativeImageResponse from(PublicRepresentativeImageResult result) {
    return new PublicRepresentativeImageResponse(
        result.id(),
        result.assetId(),
        result.deliveryUrl(),
        result.sortOrder(),
        result.variants().stream().map(Variant::from).toList());
  }

  @Override
  public List<Variant> variants() {
    return List.copyOf(variants);
  }

  public record Variant(String type, String deliveryUrl, int width, int height) {

    public static Variant from(PublicRepresentativeImageResult.Variant variant) {
      return new Variant(variant.type(), variant.deliveryUrl(), variant.width(), variant.height());
    }
  }
}
