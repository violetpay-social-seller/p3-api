package io.point3.p3api.auth.controller;

import io.point3.p3api.user.domain.type.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CompleteRegistrationRequest(
    @NotBlank String role,

    @NotBlank
    @Pattern(
        regexp = "^01[0-9]-?\\d{3,4}-?\\d{4}$",
        message = "phoneNumber must be a valid Korean mobile phone number")
    String phoneNumber) {
  public UserRole toRole() {
    return UserRole.signUpRoleOf(role);
  }
}
