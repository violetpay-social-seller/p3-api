package io.point3.p3api.orderform.controller.request;

import io.point3.p3api.orderform.domain.type.OptionInputType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record OrderFormOptionRequest(
    @NotBlank @Size(max = 100) String label,
    @NotBlank @Size(max = 100) String value,
    @NotNull OptionInputType inputType,
    @PositiveOrZero Long price,
    @Size(max = 100) String priceLabel,
    String settings,
    boolean active,
    @PositiveOrZero int sortOrder) {}
