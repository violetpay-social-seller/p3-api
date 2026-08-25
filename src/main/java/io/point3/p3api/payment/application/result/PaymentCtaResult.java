package io.point3.p3api.payment.application.result;

import java.time.Instant;
import java.util.UUID;

public record PaymentCtaResult(
    UUID inquiryId,
    UUID confirmationId,
    long amount,
    boolean canPay,
    PaymentCtaStatus status,
    String reason,
    Instant buyerViewedAt,
    PaymentAttemptResult latestPaymentAttempt) {}
