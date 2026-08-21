package io.point3.p3api.assetvariant.application.query;

import io.point3.p3api.assetvariant.application.result.RegisteredAssetVariants;
import java.util.UUID;

public interface AssetVariantQueryUseCase {
  RegisteredAssetVariants getVariants(UUID assetId);
}
