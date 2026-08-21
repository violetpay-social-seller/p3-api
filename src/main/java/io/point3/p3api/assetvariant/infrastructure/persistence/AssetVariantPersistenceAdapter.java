package io.point3.p3api.assetvariant.infrastructure.persistence;

import io.point3.p3api.assetvariant.application.port.AssetVariantPersistencePort;
import io.point3.p3api.assetvariant.domain.entity.AssetVariant;
import java.util.List;
import java.util.UUID;
import io.point3.p3api.assetvariant.domain.type.AssetVariantType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class AssetVariantPersistenceAdapter implements AssetVariantPersistencePort {

  private final AssetVariantJpaRepository assetVariantJpaRepository;

  @Override
  public List<AssetVariant> saveAll(List<AssetVariant> variants) {
    return assetVariantJpaRepository.saveAll(variants);
  }

  @Override
  public List<AssetVariant> findAllByAssetId(UUID assetId) {
    return assetVariantJpaRepository.findAllByAsset_IdOrderByWidthAsc(assetId);
  }

  @Override
  public boolean existsByAssetIdAndType(UUID assetId, AssetVariantType type) {
    return assetVariantJpaRepository.existsByAsset_IdAndType(assetId, type);
  }
}
