package io.point3.p3api.account.application.settlement;

import io.point3.p3api.account.domain.type.AccountHolderType;
import java.time.Instant;

public record SellerSettlementAccountResult(
    String bankCode,
    String bankName,
    String accountNumberMasked,
    String accountHolderName,
    AccountHolderType holderType,
    Instant verifiedAt) {}
