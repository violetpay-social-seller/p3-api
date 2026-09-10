package io.point3.p3api.seller.application.reapply;

import java.util.UUID;

public record ReapplySellerOnboardingCommand(
    UUID onboardingId,
    UUID applicantUserId,
    String storeName,
    String phoneNumber,
    String address,
    String detailAddress,
    String snsLink) {

  public static ReapplySellerOnboardingCommand from(
      UUID onboardingId,
      UUID applicantUserId,
      String storeName,
      String phoneNumber,
      String address,
      String snsLink) {
    return from(
        onboardingId, applicantUserId, storeName, phoneNumber, address, null, snsLink);
  }

  public static ReapplySellerOnboardingCommand from(
      UUID onboardingId,
      UUID applicantUserId,
      String storeName,
      String phoneNumber,
      String address,
      String detailAddress,
      String snsLink) {
    return new ReapplySellerOnboardingCommand(
        onboardingId, applicantUserId, storeName, phoneNumber, address, detailAddress, snsLink);
  }
}
