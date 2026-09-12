package io.point3.p3api.account.infrastructure.external.temporary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.point3.p3api.account.application.port.AccountRealNameVerificationRequest;
import io.point3.p3api.account.application.port.AccountRealNameVerificationResult;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TemporaryAccountRealNameVerificationAdapterTest {

  private static final Instant NOW = Instant.parse("2026-09-12T02:00:00Z");

  @Test
  @DisplayName("금융결제원 호출 없이 입력값 기반 임시 실명확인 결과를 반환한다")
  void returnsInputBasedTemporaryResult() {
    TemporaryAccountRealNameVerificationAdapter adapter =
        new TemporaryAccountRealNameVerificationAdapter(Clock.fixed(NOW, ZoneOffset.UTC));
    AccountRealNameVerificationRequest request =
        new AccountRealNameVerificationRequest("011", "14112592492", "유의진", " ", "001222");

    AccountRealNameVerificationResult result = adapter.verify(request);

    assertTrue(result.providerTransactionId().startsWith("TEMP"));
    assertTrue(result.providerTransactionId().length() <= 40);
    assertEquals("011", result.bankCode());
    assertEquals("14112592492", result.accountNumber());
    assertEquals("유의진", result.accountHolderName());
    assertEquals("TEMPORARY", result.accountType());
    assertEquals(NOW, result.verifiedAt());
  }
}
