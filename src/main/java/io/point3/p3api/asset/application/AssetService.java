package io.point3.p3api.asset.application;

import io.point3.p3api.asset.application.register.AssetRegisterUseCase;
import io.point3.p3api.asset.application.register.AssetStoragePort;
import io.point3.p3api.asset.application.register.RegisterAssetCommand;
import io.point3.p3api.asset.application.register.StoreAssetCommand;
import io.point3.p3api.asset.application.storage.AssetStorageKeyGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssetService implements AssetRegisterUseCase {

    private final AssetStoragePort assetStoragePort;

    @Override
    public RegistryAsset register(RegisterAssetCommand command) {
        UUID assetId = UUID.randomUUID();
        String objectKey = AssetStorageKeyGenerator.original(assetId, command.originalFilename());

        assetStoragePort.store(StoreAssetCommand.from(command));
        return null;
    }
}
