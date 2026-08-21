package io.point3.p3api.seller.controller;

import io.point3.p3api.auth.infrastructure.security.RoleGuard;
import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.seller.application.query.SellerOnboardingPendingQueryUseCase;
import io.point3.p3api.seller.controller.response.OperatorSellerOnboardingResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/operator/seller/applications")
@RequiredArgsConstructor
public class OperatorSellerOnboardingController {

  private final SellerOnboardingPendingQueryUseCase sellerOnboardingPendingQueryUseCase;

  @GetMapping
  public ApiResponse<List<OperatorSellerOnboardingResponse>> getPendingOnboardings(
      @Authenticated CurrentUser currentUser) {
    RoleGuard.requireOperator(currentUser);

    return ApiResponse.ok(sellerOnboardingPendingQueryUseCase.getPendingOnboardings().stream()
        .map(OperatorSellerOnboardingResponse::from)
        .toList());
  }
}
