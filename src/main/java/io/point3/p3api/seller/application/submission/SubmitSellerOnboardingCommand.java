package io.point3.p3api.seller.application.submission;

import io.point3.p3api.user.application.registration.CompleteRegistrationCommand;
import java.util.Objects;

public record SubmitSellerOnboardingCommand(
    CompleteRegistrationCommand registrationCommand,
    String storeName,
    String phoneNumber,
    String address,
    String detailAddress,
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
    return of(registrationCommand, storeName, phoneNumber, address, null, snsLink);
  }

  public static SubmitSellerOnboardingCommand of(
      CompleteRegistrationCommand registrationCommand,
      String storeName,
      String phoneNumber,
      String address,
      String detailAddress,
      String snsLink) {
    return new SubmitSellerOnboardingCommand(
        registrationCommand, storeName, phoneNumber, address, detailAddress, snsLink);
  }
}
