package io.point3.p3api.asset.application.storage;


import io.point3.p3api.asset.application.register.RegisterAssetCommand;
import java.io.InputStream;
import java.util.Objects;

import static io.point3.p3api.common.validation.DomainValidator.requireText;

public record StoreAssetCommand(
        InputStream inputStream,
        String objectKey,
        String contentType,
        long sizeBytes
) {
    public StoreAssetCommand {
        inputStream = Objects.requireNonNull(inputStream, "inputStream must not be null");
        objectKey = requireText(objectKey, "storageKey");
        contentType = requireText(contentType, "contentType");

        if (sizeBytes <= 0) {
            throw new IllegalArgumentException();
        }
    }

    public static StoreAssetCommand from(
            RegisterAssetCommand command,
            String objectKey
    ) {
        return new StoreAssetCommand(
                command.inputStream(),
                objectKey,
                command.contentType(),
                command.sizeBytes());
    }
}
