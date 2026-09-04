package io.point3.p3api.user.domain.type;

import java.util.Arrays;
import java.util.Locale;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserRole {
  BUYER("buyer"),
  SELLER("seller"),
  OPERATOR("operator");

  private final String value;

  public static UserRole signUpRoleOf(String value) {
    UserRole userRole = of(value);

    if (userRole == OPERATOR) {
      throw new IllegalArgumentException("관리자 가입은 따로");
    }

    return userRole;
  }

  public static UserRole of(String value) {
    String normalized = value == null ? null : value.trim().toLowerCase(Locale.ROOT);

    return Arrays.stream(values())
        .filter(role -> role.value.equals(normalized))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Invalid user role: " + value));
  }
}
