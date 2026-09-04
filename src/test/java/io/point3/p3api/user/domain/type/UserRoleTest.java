package io.point3.p3api.user.domain.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserRoleTest {

  @Test
  @DisplayName("역할 문자열은 대소문자와 앞뒤 공백을 정규화해 해석한다")
  void parsesRoleIgnoringCaseAndSpaces() {
    assertEquals(UserRole.BUYER, UserRole.of("BUYER"));
    assertEquals(UserRole.SELLER, UserRole.of(" seller "));
  }

  @Test
  @DisplayName("지원하지 않는 역할 문자열은 거절한다")
  void rejectsUnsupportedRole() {
    assertThrows(IllegalArgumentException.class, () -> UserRole.of("admin"));
  }
}
