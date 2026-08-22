package io.point3.p3api.seller.controller.response;

import io.point3.p3api.seller.application.result.SellerOnboardingResult;
import io.point3.p3api.seller.domain.type.SellerOnboardingStatus;
import java.time.Instant;
import java.util.UUID;

public record SellerOnboardingResponse(UUID id, SellerOnboardingStatus status, Instant createdAt) {

  public static SellerOnboardingResponse from(SellerOnboardingResult result) {
    return new SellerOnboardingResponse(result.id(), result.status(), result.createdAt());
  }
}
