package io.point3.p3api.user.application.registration;

import io.point3.p3api.user.domain.type.UserRole;

public record CompleteRegistrationCommand(
    String cognitoSub, String email, String name, UserRole role) {

  public static CompleteRegistrationCommand of(
      String cognitoSub, String email, String name, UserRole role) {
    return new CompleteRegistrationCommand(cognitoSub, email, name, role);
  }
}
