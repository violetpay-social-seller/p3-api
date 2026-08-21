package io.point3.p3api.seller.controller.response;

import io.point3.p3api.seller.application.result.SellerOnboardingDetailResult;
import io.point3.p3api.seller.domain.type.SellerOnboardingStatus;
import java.time.Instant;
import java.util.UUID;

public record SellerOnboardingCurrentResponse(
    UUID id,
    String storeName,
    String phoneNumber,
    String address,
    String snsLink,
    SellerOnboardingStatus status,
    String rejectionReason,
    Instant reviewedAt,
    Instant createdAt) {

  public static SellerOnboardingCurrentResponse from(SellerOnboardingDetailResult result) {
    String rejectionReason = result.status() == SellerOnboardingStatus.REJECTED
        ? result.rejectionReason()
        : null;

    return new SellerOnboardingCurrentResponse(
        result.id(),
        result.storeName(),
        result.phoneNumber(),
        result.address(),
        result.snsLink(),
        result.status(),
        rejectionReason,
        result.reviewedAt(),
        result.createdAt());
  }
}
