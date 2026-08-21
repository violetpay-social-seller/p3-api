package io.point3.p3api.seller.application.result;

import io.point3.p3api.seller.domain.entity.SellerOnboarding;
import io.point3.p3api.seller.domain.type.SellerOnboardingStatus;
import java.time.Instant;
import java.util.UUID;

public record SellerOnboardingResult(
    UUID id,
    UUID applicantUserId,
    String storeName,
    String phoneNumber,
    String address,
    String snsLink,
    SellerOnboardingStatus status,
    Instant createdAt) {

  public static SellerOnboardingResult from(SellerOnboarding sellerOnboarding) {
    return new SellerOnboardingResult(
        sellerOnboarding.getId(),
        sellerOnboarding.getApplicantUserId(),
        sellerOnboarding.getStoreName(),
        sellerOnboarding.getPhoneNumber(),
        sellerOnboarding.getAddress(),
        sellerOnboarding.getSnsLink(),
        sellerOnboarding.getStatus(),
        sellerOnboarding.getCreatedAt());
  }
}
