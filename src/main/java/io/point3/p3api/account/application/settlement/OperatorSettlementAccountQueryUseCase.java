package io.point3.p3api.account.application.settlement;

import java.util.UUID;

public interface OperatorSettlementAccountQueryUseCase {

  OperatorSettlementAccountResult getForOperator(UUID storeId);
}
