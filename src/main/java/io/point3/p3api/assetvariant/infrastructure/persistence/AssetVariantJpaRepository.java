package io.point3.p3api.assetvariant.infrastructure.persistence;

import io.point3.p3api.assetvariant.domain.entity.AssetVariant;
import io.point3.p3api.assetvariant.domain.type.AssetVariantType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetVariantJpaRepository extends JpaRepository<AssetVariant, UUID> {
  List<AssetVariant> findAllByAsset_IdOrderByWidthAsc(UUID assetId);

  List<AssetVariant> findAllByAsset_IdInOrderByWidthAsc(List<UUID> assetIds);

  boolean existsByAsset_IdAndType(UUID assetId, AssetVariantType type);
}
