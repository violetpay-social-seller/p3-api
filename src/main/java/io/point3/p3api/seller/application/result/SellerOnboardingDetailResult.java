package io.point3.p3api.seller.application.result;

import io.point3.p3api.seller.domain.entity.SellerOnboarding;
import io.point3.p3api.seller.domain.type.SellerOnboardingStatus;
import java.time.Instant;
import java.util.UUID;

public record SellerOnboardingDetailResult(
    UUID id,
    UUID applicantUserId,
    String storeName,
    String phoneNumber,
    String address,
    String snsLink,
    SellerOnboardingStatus status,
    String rejectionReason,
    Instant reviewedAt,
    Instant createdAt) {

  public static SellerOnboardingDetailResult from(SellerOnboarding sellerOnboarding) {
    return new SellerOnboardingDetailResult(
        sellerOnboarding.getId(),
        sellerOnboarding.getApplicantUserId(),
        sellerOnboarding.getStoreName(),
        sellerOnboarding.getPhoneNumber(),
        sellerOnboarding.getAddress(),
        sellerOnboarding.getSnsLink(),
        sellerOnboarding.getStatus(),
        sellerOnboarding.getRejectionReason(),
        sellerOnboarding.getReviewedAt(),
        sellerOnboarding.getCreatedAt());
  }
}
