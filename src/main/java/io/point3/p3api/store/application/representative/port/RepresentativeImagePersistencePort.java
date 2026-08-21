package io.point3.p3api.store.application.representative.port;

import io.point3.p3api.store.domain.entity.StoreRepresentativeImage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepresentativeImagePersistencePort {
  StoreRepresentativeImage save(StoreRepresentativeImage image);

  Optional<StoreRepresentativeImage> findByIdAndStoreId(UUID imageId, UUID storeId);

  List<StoreRepresentativeImage> findAllByStoreId(UUID storeId);

  List<StoreRepresentativeImage> findActiveByStoreId(UUID storeId);

  long countByStoreId(UUID storeId);

  void delete(StoreRepresentativeImage image);
}
