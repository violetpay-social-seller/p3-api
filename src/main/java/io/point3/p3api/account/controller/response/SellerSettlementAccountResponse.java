package io.point3.p3api.account.controller.response;

import io.point3.p3api.account.application.settlement.SellerSettlementAccountResult;
import io.point3.p3api.account.domain.type.AccountHolderType;
import java.time.Instant;

public record SellerSettlementAccountResponse(
    String bankCode,
    String bankName,
    String accountNumberMasked,
    String accountHolderName,
    AccountHolderType holderType,
    String verificationStatus,
    Instant verifiedAt) {

  private static final String VERIFIED = "VERIFIED";

  public static SellerSettlementAccountResponse from(SellerSettlementAccountResult result) {
    return new SellerSettlementAccountResponse(
        result.bankCode(),
        result.bankName(),
        result.accountNumberMasked(),
        result.accountHolderName(),
        result.holderType(),
        VERIFIED,
        result.verifiedAt());
  }
}
