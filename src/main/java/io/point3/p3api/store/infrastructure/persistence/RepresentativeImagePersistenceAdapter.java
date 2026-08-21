package io.point3.p3api.store.infrastructure.persistence;

import io.point3.p3api.store.application.representative.port.RepresentativeImagePersistencePort;
import io.point3.p3api.store.domain.entity.StoreRepresentativeImage;
import io.point3.p3api.store.domain.type.StoreRepresentativeImageStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class RepresentativeImagePersistenceAdapter implements RepresentativeImagePersistencePort {
  private final RepresentativeImageJpaRepository representativeImageJpaRepository;

  @Override
  public StoreRepresentativeImage save(StoreRepresentativeImage image) {
    return representativeImageJpaRepository.save(image);
  }

  @Override
  public Optional<StoreRepresentativeImage> findByIdAndStoreId(UUID imageId, UUID storeId) {
    return representativeImageJpaRepository.findByIdAndStoreId(imageId, storeId);
  }

  @Override
  public List<StoreRepresentativeImage> findAllByStoreId(UUID storeId) {
    return representativeImageJpaRepository.findAllByStoreIdOrderBySortOrderAsc(storeId);
  }

  @Override
  public List<StoreRepresentativeImage> findActiveByStoreId(UUID storeId) {
    return representativeImageJpaRepository.findAllByStoreIdAndStatusOrderBySortOrderAsc(
        storeId, StoreRepresentativeImageStatus.ACTIVE);
  }

  @Override
  public long countByStoreId(UUID storeId) {
    return representativeImageJpaRepository.countByStoreId(storeId);
  }

  @Override
  public void delete(StoreRepresentativeImage image) {
    representativeImageJpaRepository.delete(image);
  }
}
