package io.point3.p3api.account.application.port;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AccountRealNameVerificationRequestTest {

  @Test
  @DisplayName("금융결제원 실명번호 구분코드의 공백 한 글자를 허용한다")
  void acceptsBlankHolderInfoTypeCode() {
    assertDoesNotThrow(
        () -> new AccountRealNameVerificationRequest("004", "123456789012", "홍길동", " ", "900101"));
  }

  @Test
  @DisplayName("금융결제원 명세에 없는 실명번호 구분코드를 거부한다")
  void rejectsUnsupportedHolderInfoTypeCode() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new AccountRealNameVerificationRequest("004", "123456789012", "홍길동", "A", "900101"));
  }

  @Test
  @DisplayName("은행 코드와 계좌번호 형식을 검증한다")
  void rejectsInvalidBankAndAccountNumber() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new AccountRealNameVerificationRequest("04", "123456789012", "홍길동", " ", "900101"));
    assertThrows(
        IllegalArgumentException.class,
        () -> new AccountRealNameVerificationRequest("004", "1234A6789012", "홍길동", " ", "900101"));
  }
}
