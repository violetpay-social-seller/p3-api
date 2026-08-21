package io.point3.p3api.seller.controller.response;

import io.point3.p3api.seller.application.result.SellerOnboardingResult;
import java.time.Instant;
import java.util.UUID;

public record OperatorSellerOnboardingResponse(
    UUID id,
    UUID applicantUserId,
    String storeName,
    String phoneNumber,
    String address,
    String snsLink,
    Instant createdAt) {

  public static OperatorSellerOnboardingResponse from(SellerOnboardingResult result) {
    return new OperatorSellerOnboardingResponse(
        result.id(),
        result.applicantUserId(),
        result.storeName(),
        result.phoneNumber(),
        result.address(),
        result.snsLink(),
        result.createdAt());
  }
}
