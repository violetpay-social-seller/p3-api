package io.point3.p3api.user.domain.type;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum UserRole {
  BUYER("buyer"),
  SELLER("seller"),
  OPERATOR("operator");

  private final String value;
  public static UserRole of(String value) {
    return Arrays.stream(values())
            .filter(role -> role.value.equals(value))
            .findFirst()
            .orElseThrow(() ->
                    new IllegalArgumentException("Invalid order status: " + value)
            );
  }
}
