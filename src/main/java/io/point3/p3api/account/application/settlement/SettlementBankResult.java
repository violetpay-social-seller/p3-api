package io.point3.p3api.account.application.settlement;

import io.point3.p3api.account.domain.type.SettlementBank;

public record SettlementBankResult(String code, String name) {

  public static SettlementBankResult from(SettlementBank bank) {
    return new SettlementBankResult(bank.code(), bank.displayName());
  }
}
