package io.point3.p3api.user.application.result;

import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.SignupProvider;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.domain.type.UserStatus;
import java.util.UUID;

public record UserProfileResult(
    UUID userId,
    String email,
    String phoneNumber,
    SignupProvider signupProvider,
    String name,
    UserRole role,
    UserStatus status,
    String nextRoute) {

  public static UserProfileResult from(User user) {
    return new UserProfileResult(
        user.getId(),
        user.getEmail(),
        user.getPhoneNumber(),
        user.getSignupProvider(),
        user.getName(),
        user.getRole(),
        user.getStatus(),
        nextRoute(user.getRole()));
  }

  private static String nextRoute(UserRole role) {
    return switch (role) {
      case BUYER -> "BUYER_HOME";
      case SELLER -> "SELLER_HOME";
      case OPERATOR -> "ADMIN_HOME";
    };
  }
}
