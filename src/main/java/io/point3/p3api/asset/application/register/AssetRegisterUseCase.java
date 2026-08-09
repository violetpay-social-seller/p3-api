package io.point3.p3api.asset.application.register;

import io.point3.p3api.asset.application.result.RegistryAsset;

public interface AssetRegisterUseCase {

  RegistryAsset register(RegisterAssetCommand command);
}
