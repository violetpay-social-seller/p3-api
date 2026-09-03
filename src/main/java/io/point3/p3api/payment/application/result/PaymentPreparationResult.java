package io.point3.p3api.payment.application.result;

import java.time.Instant;
import java.util.UUID;

public record PaymentPreparationResult(
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
    Instant expiresAt) {}
