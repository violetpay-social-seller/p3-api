package io.point3.p3api.seller.application.create;

import java.util.UUID;

public record CreateSellerOnboardingCommand(
    UUID applicantUserId, String storeName, String phoneNumber, String address, String snsLinks) {

  public static CreateSellerOnboardingCommand from(
      UUID applicantUserId,
      String storeName,
      String phoneNumber,
      String address,
      String snsLinks) {
    return new CreateSellerOnboardingCommand(
        applicantUserId, storeName, phoneNumber, address, snsLinks);
  }
}
