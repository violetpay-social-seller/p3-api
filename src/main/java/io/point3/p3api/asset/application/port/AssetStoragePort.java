package io.point3.p3api.asset.application.port;

import io.point3.p3api.asset.application.storage.StoreAssetCommand;

public interface AssetStoragePort {

    void store(StoreAssetCommand command);

}
