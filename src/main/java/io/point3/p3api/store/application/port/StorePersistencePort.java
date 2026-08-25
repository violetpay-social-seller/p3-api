package io.point3.p3api.store.application.port;

import io.point3.p3api.store.domain.entity.Store;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StorePersistencePort {

  Store save(Store store);

  Optional<Store> findById(UUID storeId);

  Optional<Store> findBySlug(String slug);

  Optional<Store> findByOwnerUserId(UUID ownerUserId);

  List<Store> findActiveStores(Instant cursorUpdatedAt, UUID cursorId, int limit);

  boolean existsByOwnerUserId(UUID ownerUserId);

  boolean existsBySlug(String slug);
}
