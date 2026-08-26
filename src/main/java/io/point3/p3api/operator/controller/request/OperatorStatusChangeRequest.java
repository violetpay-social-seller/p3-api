package io.point3.p3api.operator.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OperatorStatusChangeRequest<T>(
    @NotNull T status, @NotBlank String reason) {}
