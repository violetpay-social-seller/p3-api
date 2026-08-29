package io.point3.p3api.orderform.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record OrderFormFieldOptionRequest(
    @NotBlank @Size(max = 100) String label,
    @NotBlank @Size(max = 100) String value,
    @NotNull @PositiveOrZero Long price,
    boolean active,
    @PositiveOrZero int sortOrder) {}
