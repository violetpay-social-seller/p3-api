package io.point3.p3api.seller.application.review;

import io.point3.p3api.seller.application.result.SellerOnboardingReviewResult;

public interface SellerOnboardingReviewUseCase {

  SellerOnboardingReviewResult approve(ApproveSellerOnboardingCommand command);

  SellerOnboardingReviewResult reject(RejectSellerOnboardingCommand command);
}
