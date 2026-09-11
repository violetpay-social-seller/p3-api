package io.point3.p3api.account.application.settlement;

public interface SellerSettlementAccountRegisterUseCase {

  SellerSettlementAccountResult register(RegisterSellerSettlementAccountCommand command);
}
