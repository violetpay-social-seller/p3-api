package io.point3.p3api.payment.controller.request;

import jakarta.validation.constraints.NotBlank;

public record PaymentCaptureRequest(
    @NotBlank String sessionId, @NotBlank String payerId) {}
