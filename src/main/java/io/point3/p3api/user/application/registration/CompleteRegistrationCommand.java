package io.point3.p3api.user.application.registration;

import io.point3.p3api.user.domain.type.SignupProvider;
import io.point3.p3api.user.domain.type.UserRole;
import java.util.Objects;

public record CompleteRegistrationCommand(
    String cognitoSub,
    String email,
    String name,
    UserRole role,
    String phoneNumber,
    SignupProvider signupProvider) {

  public CompleteRegistrationCommand {
    Objects.requireNonNull(cognitoSub, "cognitoSub");
    Objects.requireNonNull(email, "email");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(role, "role");
    Objects.requireNonNull(phoneNumber, "phoneNumber");
    Objects.requireNonNull(signupProvider, "signupProvider");
    if (phoneNumber.isBlank()) {
      throw new IllegalArgumentException("phoneNumber must not be blank");
    }
  }

  public static CompleteRegistrationCommand of(
      String cognitoSub,
      String email,
      String name,
      UserRole role,
      String phoneNumber,
      SignupProvider signupProvider) {
    return new CompleteRegistrationCommand(
        cognitoSub, email, name, role, phoneNumber, signupProvider);
  }
}
