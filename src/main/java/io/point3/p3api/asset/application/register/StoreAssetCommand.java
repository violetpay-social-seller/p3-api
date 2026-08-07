package io.point3.p3api.asset.application.register;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.io.InputStream;
import java.util.Objects;

import static io.point3.p3api.common.validation.DomainValidator.requireText;

public record StoreAssetCommand(
        InputStream inputStream,
        String storageKey,
        String contentType,
        long sizeBytes
) {
    public StoreAssetCommand {
        inputStream = Objects.requireNonNull(inputStream, "inputStream must not be null");
        storageKey = requireText(storageKey, "storageKey");
        contentType = requireText(contentType, "contentType");

        if (sizeBytes <= 0) {
            throw new IllegalArgumentException();
        }
    }

    public static StoreAssetCommand from(
            RegisterAssetCommand command
    ) {
        return new StoreAssetCommand(
                command.inputStream(),
                 storageKey, contentType, sizeBytes);
    }
}
