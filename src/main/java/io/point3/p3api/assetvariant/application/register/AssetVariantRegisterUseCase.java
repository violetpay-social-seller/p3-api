package io.point3.p3api.assetvariant.application.register;

import io.point3.p3api.assetvariant.application.result.RegisteredAssetVariants;

public interface AssetVariantRegisterUseCase {

    RegisteredAssetVariants register(RegisterAssetVariantsCommand command);
}
