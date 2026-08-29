package io.point3.p3api.auth.controller;

import io.point3.p3api.user.application.result.UserProfileResult;
import io.point3.p3api.user.domain.type.SignupProvider;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.domain.type.UserStatus;
import java.util.UUID;

public record UserProfileResponse(
    UUID userId,
    String email,
    String phoneNumber,
    SignupProvider signupProvider,
    String name,
    UserRole role,
    UserStatus status,
    String nextRoute) {

  public static UserProfileResponse from(UserProfileResult result) {
    return new UserProfileResponse(
        result.userId(),
        result.email(),
        result.phoneNumber(),
        result.signupProvider(),
        result.name(),
        result.role(),
        result.status(),
        result.nextRoute());
  }
}
