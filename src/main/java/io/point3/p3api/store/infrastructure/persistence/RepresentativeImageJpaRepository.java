package io.point3.p3api.store.infrastructure.persistence;

import io.point3.p3api.store.domain.entity.StoreRepresentativeImage;
import io.point3.p3api.store.domain.type.StoreRepresentativeImageStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepresentativeImageJpaRepository
    extends JpaRepository<StoreRepresentativeImage, UUID> {
  Optional<StoreRepresentativeImage> findByIdAndStoreId(UUID imageId, UUID storeId);

  List<StoreRepresentativeImage> findAllByStoreIdOrderBySortOrderAsc(UUID storeId);

  List<StoreRepresentativeImage> findAllByStoreIdAndStatusOrderBySortOrderAsc(
      UUID storeId, StoreRepresentativeImageStatus status);

  long countByStoreId(UUID storeId);
}
