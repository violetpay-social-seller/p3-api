package io.point3.p3api.order.controller.request;

import jakarta.validation.constraints.NotBlank;

public record OrderCancelRequest(@NotBlank String reason) {}
