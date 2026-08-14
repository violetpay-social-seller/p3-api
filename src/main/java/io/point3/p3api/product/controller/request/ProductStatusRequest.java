package io.point3.p3api.product.controller.request;

import io.point3.p3api.product.domain.type.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record ProductStatusRequest(@NotNull ProductStatus status) {}
