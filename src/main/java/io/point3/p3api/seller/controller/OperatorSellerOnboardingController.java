package io.point3.p3api.seller.controller;

import io.point3.p3api.auth.infrastructure.security.RoleGuard;
import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.seller.application.query.SellerOnboardingPendingQueryUseCase;
import io.point3.p3api.seller.application.review.ApproveSellerOnboardingCommand;
import io.point3.p3api.seller.application.review.RejectSellerOnboardingCommand;
import io.point3.p3api.seller.application.review.SellerOnboardingReviewUseCase;
import io.point3.p3api.seller.application.result.SellerOnboardingReviewResult;
import io.point3.p3api.seller.controller.request.SellerOnboardingRejectRequest;
import io.point3.p3api.seller.controller.response.OperatorSellerOnboardingResponse;
import io.point3.p3api.seller.controller.response.SellerOnboardingReviewResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/operator/seller-onboardings")
@RequiredArgsConstructor
public class OperatorSellerOnboardingController {

  private final SellerOnboardingPendingQueryUseCase sellerOnboardingPendingQueryUseCase;
  private final SellerOnboardingReviewUseCase sellerOnboardingReviewUseCase;

  @GetMapping
  public ApiResponse<List<OperatorSellerOnboardingResponse>> getPendingOnboardings(
      @Authenticated CurrentUser currentUser) {
    RoleGuard.requireOperator(currentUser);

    return ApiResponse.ok(sellerOnboardingPendingQueryUseCase.getPendingOnboardings().stream()
        .map(OperatorSellerOnboardingResponse::from)
        .toList());
  }

  @PatchMapping("/{onboardingId}/approve")
  public ApiResponse<SellerOnboardingReviewResponse> approve(
      @PathVariable UUID onboardingId, @Authenticated CurrentUser currentUser) {
    RoleGuard.requireOperator(currentUser);

    SellerOnboardingReviewResult result = sellerOnboardingReviewUseCase.approve(
        ApproveSellerOnboardingCommand.from(onboardingId, currentUser.userId()));
    return ApiResponse.ok(SellerOnboardingReviewResponse.from(result));
  }

  @PatchMapping("/{onboardingId}/reject")
  public ApiResponse<SellerOnboardingReviewResponse> reject(
      @PathVariable UUID onboardingId,
      @Authenticated CurrentUser currentUser,
      @Valid @RequestBody SellerOnboardingRejectRequest request) {
    RoleGuard.requireOperator(currentUser);

    SellerOnboardingReviewResult result = sellerOnboardingReviewUseCase.reject(
        RejectSellerOnboardingCommand.from(
            onboardingId, currentUser.userId(), request.rejectionReason()));
    return ApiResponse.ok(SellerOnboardingReviewResponse.from(result));
  }
}
