package io.point3.p3api.store.application.representative.query;

import io.point3.p3api.store.application.representative.result.RepresentativeImageResult;
import java.util.List;
import java.util.UUID;

public interface RepresentativeImageQueryUseCase {
  List<RepresentativeImageResult> getSellerImages(UUID storeId);

  RepresentativeImageResult getSellerImage(UUID storeId, UUID imageId);

  List<RepresentativeImageResult> getActiveImages(UUID storeId);
}
