package io.point3.p3api.user.domain.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserRoleTest {

  @Test
  @DisplayName("가입 역할은 프론트 계약의 대문자 role 값을 허용한다")
  void parsesUppercaseSignupRole() {
    assertEquals(UserRole.BUYER, UserRole.signUpRoleOf("BUYER"));
    assertEquals(UserRole.SELLER, UserRole.signUpRoleOf("SELLER"));
  }

  @Test
  @DisplayName("가입 역할은 기존 소문자 role 값도 유지한다")
  void parsesLowercaseSignupRole() {
    assertEquals(UserRole.BUYER, UserRole.signUpRoleOf("buyer"));
    assertEquals(UserRole.SELLER, UserRole.signUpRoleOf("seller"));
  }

  @Test
  @DisplayName("운영자 역할은 가입 역할로 허용하지 않는다")
  void rejectsOperatorSignupRole() {
    assertThrows(IllegalArgumentException.class, () -> UserRole.signUpRoleOf("OPERATOR"));
  }
}
