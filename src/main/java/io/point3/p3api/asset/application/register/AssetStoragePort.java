package io.point3.p3api.asset.application.register;

public interface AssetStoragePort {

    void store(StoreAssetCommand command);

}
