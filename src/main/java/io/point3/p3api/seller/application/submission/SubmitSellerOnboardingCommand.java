package io.point3.p3api.seller.application.submission;

import io.point3.p3api.user.application.registration.CompleteRegistrationCommand;
import java.util.Objects;

public record SubmitSellerOnboardingCommand(
    CompleteRegistrationCommand registrationCommand,
    String storeName,
    String phoneNumber,
    String address,
    String snsLink) {

  public SubmitSellerOnboardingCommand {
    Objects.requireNonNull(registrationCommand, "registrationCommand");
  }

  public static SubmitSellerOnboardingCommand of(
      CompleteRegistrationCommand registrationCommand,
      String storeName,
      String phoneNumber,
      String address,
      String snsLink) {
    return new SubmitSellerOnboardingCommand(
        registrationCommand, storeName, phoneNumber, address, snsLink);
  }
}
