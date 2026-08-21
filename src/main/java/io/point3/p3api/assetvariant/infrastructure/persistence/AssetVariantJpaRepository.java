package io.point3.p3api.assetvariant.infrastructure.persistence;

import io.point3.p3api.assetvariant.domain.entity.AssetVariant;
import java.util.UUID;
import java.util.List;
import io.point3.p3api.assetvariant.domain.type.AssetVariantType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetVariantJpaRepository extends JpaRepository<AssetVariant, UUID> {
  List<AssetVariant> findAllByAsset_IdOrderByWidthAsc(UUID assetId);
  boolean existsByAsset_IdAndType(UUID assetId, AssetVariantType type);
}
