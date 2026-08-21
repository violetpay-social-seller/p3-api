package io.point3.p3api.assetvariant.application.port;

import io.point3.p3api.assetvariant.domain.entity.AssetVariant;
import java.util.List;
import java.util.UUID;
import io.point3.p3api.assetvariant.domain.type.AssetVariantType;

public interface AssetVariantPersistencePort {

  List<AssetVariant> saveAll(List<AssetVariant> variants);

  List<AssetVariant> findAllByAssetId(UUID assetId);

  boolean existsByAssetIdAndType(UUID assetId, AssetVariantType type);
}
