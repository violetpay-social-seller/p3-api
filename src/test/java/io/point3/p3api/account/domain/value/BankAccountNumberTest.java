package io.point3.p3api.account.domain.value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BankAccountNumberTest {

  @Test
  @DisplayName("계좌번호의 구분자를 제거하고 마지막 네 자리만 표시한다")
  void normalizesAndMasksAccountNumber() {
    BankAccountNumber accountNumber = BankAccountNumber.of("123-456 789012");

    assertEquals("123456789012", accountNumber.value());
    assertEquals("********9012", accountNumber.masked());
    assertFalse(accountNumber.toString().contains(accountNumber.value()));
  }

  @Test
  @DisplayName("계좌번호는 최대 16자리 숫자만 허용한다")
  void rejectsInvalidAccountNumber() {
    assertThrows(IllegalArgumentException.class, () -> BankAccountNumber.of("1234A567"));
    assertThrows(IllegalArgumentException.class, () -> BankAccountNumber.of("12345678901234567"));
  }
}
