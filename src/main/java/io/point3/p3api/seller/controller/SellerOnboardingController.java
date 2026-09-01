package io.point3.p3api.seller.controller;

import io.point3.p3api.auth.JwtCommandExtractor;
import io.point3.p3api.auth.infrastructure.security.RoleGuard;
import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.seller.application.query.SellerOnboardingCurrentQueryUseCase;
import io.point3.p3api.seller.application.reapply.ReapplySellerOnboardingCommand;
import io.point3.p3api.seller.application.reapply.SellerOnboardingReapplicationUseCase;
import io.point3.p3api.seller.application.result.SellerOnboardingDetailResult;
import io.point3.p3api.seller.application.result.SellerOnboardingResult;
import io.point3.p3api.seller.application.submission.SellerOnboardingSubmissionUseCase;
import io.point3.p3api.seller.application.submission.SubmitSellerOnboardingCommand;
import io.point3.p3api.seller.controller.request.SellerOnboardingCreateRequest;
import io.point3.p3api.seller.controller.response.SellerOnboardingCurrentResponse;
import io.point3.p3api.seller.controller.response.SellerOnboardingResponse;
import io.point3.p3api.user.domain.type.UserRole;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller/onboardings")
@RequiredArgsConstructor
public class SellerOnboardingController {

  private final SellerOnboardingSubmissionUseCase sellerOnboardingSubmissionUseCase;
  private final SellerOnboardingCurrentQueryUseCase sellerOnboardingCurrentQueryUseCase;
  private final SellerOnboardingReapplicationUseCase sellerOnboardingReapplicationUseCase;
  private final JwtCommandExtractor jwtCommandExtractor;

  @GetMapping("/current")
  public ApiResponse<SellerOnboardingCurrentResponse> getCurrent(
      @Authenticated CurrentUser currentUser) {
    RoleGuard.requireSeller(currentUser);

    SellerOnboardingDetailResult result =
        sellerOnboardingCurrentQueryUseCase.getCurrentOnboarding(currentUser.userId());
    return ApiResponse.ok(SellerOnboardingCurrentResponse.from(result));
  }

  @PostMapping
  public ApiResponse<SellerOnboardingResponse> create(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SellerOnboardingCreateRequest request) {
    SellerOnboardingResult result =
        sellerOnboardingSubmissionUseCase.submit(SubmitSellerOnboardingCommand.of(
            jwtCommandExtractor.extractRegistration(jwt, UserRole.SELLER, request.phoneNumber()),
            request.storeName(),
            request.phoneNumber(),
            request.address(),
            request.snsLink()));

    return ApiResponse.ok(SellerOnboardingResponse.from(result));
  }

  @PostMapping("/{onboardingId}/resubmissions")
  public ApiResponse<SellerOnboardingResponse> reapply(
      @PathVariable UUID onboardingId,
      @Authenticated CurrentUser currentUser,
      @Valid @RequestBody SellerOnboardingCreateRequest request) {
    RoleGuard.requireSeller(currentUser);

    SellerOnboardingResult result =
        sellerOnboardingReapplicationUseCase.reapply(ReapplySellerOnboardingCommand.from(
            onboardingId,
            currentUser.userId(),
            request.storeName(),
            request.phoneNumber(),
            request.address(),
            request.snsLink()));

    return ApiResponse.ok(SellerOnboardingResponse.from(result));
  }
}
