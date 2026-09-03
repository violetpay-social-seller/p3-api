package io.point3.p3api.payment.controller.response;

import io.point3.p3api.payment.application.result.PaymentPreparationResult;
import java.time.Instant;
import java.util.UUID;

public record PaymentPreparationResponse(
    UUID paymentAttemptId,
    String sessionId,
    long amount,
    String payerId,
    String clientId,
    String orderName,
    String authnClientId,
    String authnState,
    String entryPath,
    String authenticationUrl,
    String point3PaymentOrigin,
    Instant expiresAt) {

  public static PaymentPreparationResponse from(PaymentPreparationResult result) {
    return new PaymentPreparationResponse(
        result.paymentAttemptId(),
        result.sessionId(),
        result.amount(),
        result.payerId(),
        result.clientId(),
        result.orderName(),
        result.authnClientId(),
        result.authnState(),
        result.entryPath(),
        result.authenticationUrl(),
        result.point3PaymentOrigin(),
        result.expiresAt());
  }
}
