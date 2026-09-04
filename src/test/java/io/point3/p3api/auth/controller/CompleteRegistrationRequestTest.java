package io.point3.p3api.auth.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.user.domain.type.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CompleteRegistrationRequestTest {

  @Test
  @DisplayName("가입 역할은 대문자 요청값도 허용한다")
  void acceptsUppercaseRole() {
    CompleteRegistrationRequest request = new CompleteRegistrationRequest("BUYER", "010-1234-5678");

    assertEquals(UserRole.BUYER, request.toRole());
  }

  @Test
  @DisplayName("지원하지 않는 가입 역할은 입력 오류로 거절한다")
  void rejectsUnsupportedRoleAsInvalidInput() {
    CompleteRegistrationRequest request = new CompleteRegistrationRequest("ADMIN", "010-1234-5678");

    BaseException exception = assertThrows(BaseException.class, request::toRole);

    assertEquals(CommonErrorCode.INVALID_INPUT, exception.getErrorCode());
  }
}
