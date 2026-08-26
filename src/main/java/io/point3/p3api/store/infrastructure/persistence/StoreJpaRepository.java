package io.point3.p3api.store.infrastructure.persistence;

import io.point3.p3api.store.domain.entity.Store;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoreJpaRepository
    extends JpaRepository<Store, UUID>, JpaSpecificationExecutor<Store> {

  Optional<Store> findByOwnerUserId(UUID ownerUserId);

  Optional<Store> findBySlug(String slug);

  @Query("""
      select s
      from Store s
      where s.status = io.point3.p3api.store.domain.type.StoreStatus.ACTIVE
        and (
          :cursorUpdatedAt is null
          or s.updatedAt < :cursorUpdatedAt
          or (s.updatedAt = :cursorUpdatedAt and s.id < :cursorId)
        )
      order by s.updatedAt desc, s.id desc
      """)
  List<Store> findActiveStores(
      @Param("cursorUpdatedAt") Instant cursorUpdatedAt,
      @Param("cursorId") UUID cursorId,
      Pageable pageable);

  boolean existsByOwnerUserId(UUID ownerUserId);

  boolean existsBySlug(String slug);
}
