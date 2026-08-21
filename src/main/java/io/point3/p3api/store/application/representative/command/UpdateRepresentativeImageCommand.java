package io.point3.p3api.store.application.representative.command;

import io.point3.p3api.store.domain.type.StoreRepresentativeImageStatus;
import java.util.UUID;

public record UpdateRepresentativeImageCommand(
    UUID storeId,
    UUID representativeImageId,
    int sortOrder,
    StoreRepresentativeImageStatus status) {}
