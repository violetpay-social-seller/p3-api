package io.point3.p3api.payment.controller.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record PaymentCaptureRequest(
    @JsonAlias("orderId") @NotBlank String sessionId,
    @NotBlank String payerId) {}
