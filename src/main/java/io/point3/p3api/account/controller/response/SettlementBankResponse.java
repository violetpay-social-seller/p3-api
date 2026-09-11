package io.point3.p3api.account.controller.response;

import io.point3.p3api.account.application.settlement.SettlementBankResult;

public record SettlementBankResponse(String code, String name) {

  public static SettlementBankResponse from(SettlementBankResult result) {
    return new SettlementBankResponse(result.code(), result.name());
  }
}
