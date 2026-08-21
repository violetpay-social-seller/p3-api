package io.point3.p3api.asset.application.query;

import io.point3.p3api.asset.application.result.AssetDetailResult;
import java.util.List;
import java.util.UUID;

public interface AssetQueryUseCase {
  AssetDetailResult getAsset(UUID assetId, UUID uploadedBy);

  List<AssetDetailResult> getMyAssets(UUID uploadedBy);
}
