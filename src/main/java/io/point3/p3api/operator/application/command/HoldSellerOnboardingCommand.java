package io.point3.p3api.operator.application.command;

import java.util.UUID;

public record HoldSellerOnboardingCommand(UUID onboardingId, UUID operatorUserId, String reason) {

  public static HoldSellerOnboardingCommand of(
      UUID onboardingId, UUID operatorUserId, String reason) {
    return new HoldSellerOnboardingCommand(onboardingId, operatorUserId, reason);
  }
}
