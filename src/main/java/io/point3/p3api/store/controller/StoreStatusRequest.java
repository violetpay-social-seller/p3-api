package io.point3.p3api.store.controller;

import io.point3.p3api.store.domain.type.StoreStatus;
import jakarta.validation.constraints.NotNull;

public record StoreStatusRequest(@NotNull StoreStatus status) {}
