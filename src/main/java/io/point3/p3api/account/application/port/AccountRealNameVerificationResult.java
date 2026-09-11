package io.point3.p3api.account.application.port;

import java.time.Instant;

public record AccountRealNameVerificationResult(
    String providerTransactionId,
    String bankCode,
    String bankName,
    String accountNumber,
    String accountHolderName,
    String accountType,
    Instant verifiedAt) {}
