package io.point3.p3api.store.application.port;

import io.point3.p3api.store.domain.entity.Store;
import java.util.Optional;
import java.util.UUID;

public interface StorePersistencePort {

  Store save(Store store);

  Optional<Store> findById(UUID storeId);

  Optional<Store> findByOwnerUserId(UUID ownerUserId);

  boolean existsByOwnerUserId(UUID ownerUserId);

  boolean existsBySlug(String slug);
}
