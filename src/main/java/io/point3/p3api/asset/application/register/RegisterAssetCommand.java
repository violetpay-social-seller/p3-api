package io.point3.p3api.asset.application.register;

import java.io.InputStream;
import java.util.UUID;

public record RegisterAssetCommand(UUID uuid,
                                   InputStream inputStream,
                                   String originalFilename,
                                   String contentType,
                                   long size) {
}
