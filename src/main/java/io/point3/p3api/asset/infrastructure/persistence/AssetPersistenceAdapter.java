package io.point3.p3api.asset.infrastructure.persistence;

import io.point3.p3api.asset.application.port.AssetPersistencePort;
import io.point3.p3api.asset.domain.entity.Asset;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class AssetPersistenceAdapter implements AssetPersistencePort {

  private final AssetJpaRepository assetJpaRepository;

  @Override
  public Asset save(Asset asset) {
    return assetJpaRepository.save(asset);
  }

  @Override
  public Optional<Asset> findById(UUID assetId) {
    return assetJpaRepository.findById(assetId);
  }

  @Override
  public Optional<Asset> findByIdAndUploadedBy(UUID assetId, UUID uploadedBy) {
    return assetJpaRepository.findByIdAndUploadedBy(assetId, uploadedBy);
  }

  @Override
  public List<Asset> findAllByUploadedBy(UUID uploadedBy) {
    return assetJpaRepository.findAllByUploadedByOrderByCreatedAtDesc(uploadedBy);
  }
}
