package io.point3.p3api.auth.controller;

import io.point3.p3api.user.domain.type.UserRole;
import jakarta.validation.constraints.NotBlank;

public record CompleteRegistrationRequest(@NotBlank String role) {
  public UserRole toRole() {
    return UserRole.signUpRoleOf(role);
  }
}
