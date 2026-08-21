package io.point3.p3api.store.application.representative.delete;

import java.util.UUID;

public interface RepresentativeImageDeleteUseCase {
  void delete(UUID storeId, UUID imageId);
}
