package io.point3.p3api.asset.infrastructure.s3;

import io.point3.p3api.asset.application.storage.StoreAssetCommand;

import java.io.InputStream;

public record AssetStoreRequest(
        InputStream inputStream,
        String objectKey,
        String contentType,
        long sizeBytes
) {
    public static AssetStoreRequest from(StoreAssetCommand command) {
        return new AssetStoreRequest(command.inputStream(), command.objectKey(), command.contentType(), command.sizeBytes());
    }
}
