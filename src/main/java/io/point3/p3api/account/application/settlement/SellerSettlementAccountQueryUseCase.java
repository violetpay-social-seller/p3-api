package io.point3.p3api.account.application.settlement;

import java.util.UUID;

public interface SellerSettlementAccountQueryUseCase {

  SellerSettlementAccountResult get(UUID storeId);
}
