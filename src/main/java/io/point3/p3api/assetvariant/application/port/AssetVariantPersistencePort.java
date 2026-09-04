package io.point3.p3api.assetvariant.application.port;

import io.point3.p3api.assetvariant.domain.entity.AssetVariant;
import io.point3.p3api.assetvariant.domain.type.AssetVariantStatus;
import io.point3.p3api.assetvariant.domain.type.AssetVariantType;
import java.util.List;
import java.util.UUID;

public interface AssetVariantPersistencePort {

  List<AssetVariant> saveAll(List<AssetVariant> variants);

  List<AssetVariant> findAllByAssetId(UUID assetId);

  List<AssetVariant> findAllByAssetIds(List<UUID> assetIds);

  boolean existsByAssetIdAndType(UUID assetId, AssetVariantType type);

  boolean existsByAssetIdAndStatus(UUID assetId, AssetVariantStatus status);
}
