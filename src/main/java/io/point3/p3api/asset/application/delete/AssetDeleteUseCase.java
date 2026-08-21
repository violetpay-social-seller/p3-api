package io.point3.p3api.asset.application.delete;

import java.util.UUID;

public interface AssetDeleteUseCase {
  void delete(UUID assetId, UUID uploadedBy);
}
