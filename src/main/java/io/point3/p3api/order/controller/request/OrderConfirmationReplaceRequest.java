package io.point3.p3api.order.controller.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record OrderConfirmationReplaceRequest(@NotNull UUID replacementConfirmationId) {}
