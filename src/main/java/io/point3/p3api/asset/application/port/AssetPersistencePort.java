package io.point3.p3api.asset.application.port;

import io.point3.p3api.asset.domain.entity.Asset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetPersistencePort {

  Asset save(Asset asset);

  Optional<Asset> findById(UUID assetId);

  Optional<Asset> findByIdAndUploadedBy(UUID assetId, UUID uploadedBy);

  List<Asset> findAllByUploadedBy(UUID uploadedBy);
}
