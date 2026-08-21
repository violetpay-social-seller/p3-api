package io.point3.p3api.store.application.representative.command;

import java.util.UUID;

public record CreateRepresentativeImageCommand(UUID storeId, UUID assetId, int sortOrder) {}
