package io.point3.p3api.seller.controller.response;

import io.point3.p3api.seller.application.result.SellerOnboardingReviewResult;
import io.point3.p3api.seller.domain.type.SellerOnboardingStatus;
import java.time.Instant;
import java.util.UUID;

public record SellerOnboardingReviewResponse(
    UUID id, SellerOnboardingStatus status, UUID reviewedBy, Instant reviewedAt) {

  public static SellerOnboardingReviewResponse from(SellerOnboardingReviewResult result) {
    return new SellerOnboardingReviewResponse(
        result.id(), result.status(), result.reviewedBy(), result.reviewedAt());
  }
}
