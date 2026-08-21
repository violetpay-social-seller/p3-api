package io.point3.p3api.seller.application.review;

import java.util.UUID;

public record ApproveSellerOnboardingCommand(UUID onboardingId, UUID reviewerId) {

  public static ApproveSellerOnboardingCommand from(UUID onboardingId, UUID reviewerId) {
    return new ApproveSellerOnboardingCommand(onboardingId, reviewerId);
  }
}
