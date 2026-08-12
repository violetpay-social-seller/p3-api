package io.point3.p3api.store.infrastructure.persistence;

import io.point3.p3api.store.application.port.StorePersistencePort;
import io.point3.p3api.store.domain.entity.Store;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class StorePersistenceAdapter implements StorePersistencePort {

  private final StoreJpaRepository storeJpaRepository;

  @Override
  public Store save(Store store) {
    return storeJpaRepository.save(store);
  }

  @Override
  public Optional<Store> findById(UUID storeId) {
    return storeJpaRepository.findById(storeId);
  }

  @Override
  public Optional<Store> findBySlug(String slug) {
    return storeJpaRepository.findBySlug(slug);
  }

  @Override
  public Optional<Store> findByOwnerUserId(UUID ownerUserId) {
    return storeJpaRepository.findByOwnerUserId(ownerUserId);
  }

  @Override
  public boolean existsByOwnerUserId(UUID ownerUserId) {
    return storeJpaRepository.existsByOwnerUserId(ownerUserId);
  }

  @Override
  public boolean existsBySlug(String slug) {
    return storeJpaRepository.existsBySlug(slug);
  }
}
