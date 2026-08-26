package io.point3.p3api.operator.application.result;

import io.point3.p3api.seller.domain.entity.SellerOnboarding;
import io.point3.p3api.seller.domain.type.SellerOnboardingStatus;
import java.time.Instant;
import java.util.UUID;

public record OperatorOnboardingResult(
    UUID id,
    UUID applicantUserId,
    String storeName,
    String phoneNumber,
    String address,
    String snsLink,
    SellerOnboardingStatus status,
    String rejectionReason,
    UUID reviewedBy,
    Instant reviewedAt,
    Instant createdAt,
    Instant updatedAt) {

  public static OperatorOnboardingResult from(SellerOnboarding onboarding) {
    return new OperatorOnboardingResult(
        onboarding.getId(),
        onboarding.getApplicantUserId(),
        onboarding.getStoreName(),
        onboarding.getPhoneNumber(),
        onboarding.getAddress(),
        onboarding.getSnsLink(),
        onboarding.getStatus(),
        onboarding.getRejectionReason(),
        onboarding.getReviewedBy(),
        onboarding.getReviewedAt(),
        onboarding.getCreatedAt(),
        onboarding.getUpdatedAt());
  }
}
