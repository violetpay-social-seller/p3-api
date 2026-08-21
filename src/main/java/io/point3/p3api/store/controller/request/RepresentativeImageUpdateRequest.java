package io.point3.p3api.store.controller.request;

import io.point3.p3api.store.domain.type.StoreRepresentativeImageStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RepresentativeImageUpdateRequest(
    @Min(0) int sortOrder, @NotNull StoreRepresentativeImageStatus status) {}
