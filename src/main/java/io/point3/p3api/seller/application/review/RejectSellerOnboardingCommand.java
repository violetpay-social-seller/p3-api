package io.point3.p3api.seller.application.review;

import java.util.UUID;

public record RejectSellerOnboardingCommand(
    UUID onboardingId, UUID reviewerId, String rejectionReason) {

  public static RejectSellerOnboardingCommand from(
      UUID onboardingId, UUID reviewerId, String rejectionReason) {
    return new RejectSellerOnboardingCommand(onboardingId, reviewerId, rejectionReason);
  }
}
