package io.point3.p3api.product.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductCreateRequest(
    @NotBlank @Size(max = 150) String name, String description, @PositiveOrZero Long basePrice) {}
