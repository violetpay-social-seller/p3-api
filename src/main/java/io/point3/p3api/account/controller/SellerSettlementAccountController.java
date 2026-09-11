package io.point3.p3api.account.controller;

import io.point3.p3api.account.application.settlement.SellerSettlementAccountQueryUseCase;
import io.point3.p3api.account.application.settlement.SellerSettlementAccountRegisterUseCase;
import io.point3.p3api.account.application.settlement.SettlementBankQueryUseCase;
import io.point3.p3api.account.controller.request.SellerSettlementAccountRequest;
import io.point3.p3api.account.controller.response.SellerSettlementAccountResponse;
import io.point3.p3api.account.controller.response.SettlementBankResponse;
import io.point3.p3api.common.tenant.web.CurrentStoreId;
import io.point3.p3api.common.web.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller/store/settlement-account")
@RequiredArgsConstructor
public class SellerSettlementAccountController {

  private final SellerSettlementAccountRegisterUseCase registerUseCase;
  private final SellerSettlementAccountQueryUseCase queryUseCase;
  private final SettlementBankQueryUseCase bankQueryUseCase;

  @PutMapping
  public ApiResponse<SellerSettlementAccountResponse> register(
      @CurrentStoreId UUID storeId, @Valid @RequestBody SellerSettlementAccountRequest request) {
    return ApiResponse.ok(
        SellerSettlementAccountResponse.from(registerUseCase.register(request.toCommand(storeId))));
  }

  @GetMapping
  public ApiResponse<SellerSettlementAccountResponse> get(@CurrentStoreId UUID storeId) {
    return ApiResponse.ok(SellerSettlementAccountResponse.from(queryUseCase.get(storeId)));
  }

  @GetMapping("/banks")
  public ApiResponse<List<SettlementBankResponse>> getBanks() {
    return ApiResponse.ok(
        bankQueryUseCase.getBanks().stream().map(SettlementBankResponse::from).toList());
  }
}
