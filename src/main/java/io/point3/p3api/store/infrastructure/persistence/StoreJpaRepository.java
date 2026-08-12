package io.point3.p3api.store.infrastructure.persistence;

import io.point3.p3api.store.domain.entity.Store;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreJpaRepository extends JpaRepository<Store, UUID> {

  Optional<Store> findByOwnerUserId(UUID ownerUserId);

  boolean existsByOwnerUserId(UUID ownerUserId);

  boolean existsBySlug(String slug);
}
