package io.point3.p3api.account.application.settlement;

import java.util.List;

public interface SettlementBankQueryUseCase {

  List<SettlementBankResult> getBanks();
}
