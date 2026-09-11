package io.point3.p3api.account.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.point3.p3api.account.domain.type.AccountHolderType;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SellerSettlementAccountTest {

  @Test
  @DisplayName("검증된 정산계좌 정보로 현재 계좌를 교체한다")
  void replacesVerifiedAccount() {
    SellerSettlementAccount account = account("004", "encrypted-account-1");
    Instant verifiedAt = Instant.parse("2026-09-11T03:00:00Z");

    account.replaceVerifiedAccount(
        "088",
        "encrypted-account-2",
        "encrypted-holder-2",
        AccountHolderType.BUSINESS,
        "provider-transaction-2",
        verifiedAt);

    assertEquals("088", account.getBankCode());
    assertEquals("encrypted-account-2", account.getEncryptedAccountNumber());
    assertEquals(AccountHolderType.BUSINESS, account.getAccountHolderType());
    assertEquals(verifiedAt, account.getVerifiedAt());
  }

  @Test
  @DisplayName("은행 코드는 세 자리 숫자여야 한다")
  void rejectsInvalidBankCode() {
    assertThrows(IllegalArgumentException.class, () -> account("4", "encrypted-account"));
    assertThrows(IllegalArgumentException.class, () -> account("A04", "encrypted-account"));
  }

  @Test
  @DisplayName("지원하지 않는 금융기관 코드를 거부한다")
  void rejectsUnsupportedBankCode() {
    assertThrows(IllegalArgumentException.class, () -> account("999", "encrypted-account"));
  }

  private SellerSettlementAccount account(String bankCode, String encryptedAccountNumber) {
    return SellerSettlementAccount.create(
        UUID.randomUUID(),
        bankCode,
        encryptedAccountNumber,
        "encrypted-holder",
        AccountHolderType.PERSONAL,
        "provider-transaction-1",
        Instant.parse("2026-09-11T02:00:00Z"));
  }
}
