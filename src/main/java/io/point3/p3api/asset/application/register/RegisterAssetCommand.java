package io.point3.p3api.asset.application.register;

import java.io.InputStream;
import java.util.UUID;

public record RegisterAssetCommand(
    UUID uploadedBy,
    InputStream inputStream,
    String originalFilename,
    String contentType,
    long sizeBytes) {}
