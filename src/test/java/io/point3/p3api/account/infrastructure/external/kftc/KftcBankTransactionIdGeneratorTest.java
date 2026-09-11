package io.point3.p3api.account.infrastructure.external.kftc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.point3.p3api.account.application.port.AccountRealNameVerificationException;
import java.security.SecureRandom;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KftcBankTransactionIdGeneratorTest {

  @Test
  @DisplayName("이용기관 코드와 요청 구분자를 포함한 20자리 거래고유번호를 생성한다")
  void generatesBankTransactionId() {
    SecureRandom random = mock(SecureRandom.class);
    when(random.nextInt(anyInt())).thenReturn(1);
    KftcBankTransactionIdGenerator generator =
        new KftcBankTransactionIdGenerator(properties("F123456789"), random);

    String transactionId = generator.generate();

    assertEquals("F123456789U111111111", transactionId);
    assertEquals(20, transactionId.length());
  }

  @Test
  @DisplayName("이용기관 코드 형식이 잘못되면 거래고유번호를 생성하지 않는다")
  void rejectsInvalidUseOrgCode() {
    KftcBankTransactionIdGenerator generator =
        new KftcBankTransactionIdGenerator(properties("short"));

    AccountRealNameVerificationException exception =
        assertThrows(AccountRealNameVerificationException.class, generator::generate);

    assertEquals(AccountRealNameVerificationException.Type.CONFIGURATION, exception.getType());
  }

  private KftcProperties properties(String useOrgCode) {
    return new KftcProperties(
        "https://auth.example.test",
        "https://api.example.test",
        "client-id",
        "client-secret",
        useOrgCode,
        Duration.ofSeconds(1),
        Duration.ofSeconds(30));
  }
}
