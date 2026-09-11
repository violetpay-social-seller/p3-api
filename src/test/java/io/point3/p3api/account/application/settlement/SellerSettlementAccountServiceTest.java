package io.point3.p3api.account.application.settlement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.point3.p3api.account.application.port.AccountRealNameVerificationException;
import io.point3.p3api.account.application.port.AccountRealNameVerificationPort;
import io.point3.p3api.account.application.port.AccountRealNameVerificationRequest;
import io.point3.p3api.account.application.port.AccountRealNameVerificationResult;
import io.point3.p3api.account.application.port.SensitiveDataCipher;
import io.point3.p3api.account.application.settlement.port.SellerSettlementAccountPersistencePort;
import io.point3.p3api.account.domain.entity.SellerSettlementAccount;
import io.point3.p3api.account.domain.type.AccountHolderType;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.AccountErrorCode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SellerSettlementAccountServiceTest {

  private final SellerSettlementAccountPersistencePort persistencePort =
      mock(SellerSettlementAccountPersistencePort.class);
  private final AccountRealNameVerificationPort verificationPort =
      mock(AccountRealNameVerificationPort.class);
  private final SensitiveDataCipher cipher = new TestSensitiveDataCipher();
  private final SellerSettlementAccountService service =
      new SellerSettlementAccountService(persistencePort, verificationPort, cipher);

  @BeforeEach
  void setUp() {
    when(persistencePort.findByStoreId(any())).thenReturn(Optional.empty());
    when(persistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  @DisplayName("개인 정산계좌를 실명조회한 뒤 암호화하여 저장한다")
  void registersPersonalSettlementAccount() {
    UUID storeId = UUID.randomUUID();
    Instant verifiedAt = Instant.parse("2026-09-11T01:00:00Z");
    when(verificationPort.verify(any()))
        .thenReturn(new AccountRealNameVerificationResult(
            "transaction-id", "004", "국민은행", "123456789012", "홍길동", "1", verifiedAt));

    SellerSettlementAccountResult result =
        service.register(new RegisterSellerSettlementAccountCommand(
            storeId,
            "004",
            "123-456-789012",
            " 홍길동 ",
            AccountHolderType.PERSONAL,
            LocalDate.of(1990, 1, 2),
            null));

    ArgumentCaptor<AccountRealNameVerificationRequest> requestCaptor =
        ArgumentCaptor.forClass(AccountRealNameVerificationRequest.class);
    verify(verificationPort).verify(requestCaptor.capture());
    assertEquals("900102", requestCaptor.getValue().accountHolderInfo());
    assertEquals(" ", requestCaptor.getValue().accountHolderInfoType());

    ArgumentCaptor<SellerSettlementAccount> accountCaptor =
        ArgumentCaptor.forClass(SellerSettlementAccount.class);
    verify(persistencePort).save(accountCaptor.capture());
    assertEquals("encrypted:123456789012", accountCaptor.getValue().getEncryptedAccountNumber());
    assertEquals("encrypted:홍길동", accountCaptor.getValue().getEncryptedAccountHolderName());
    assertEquals("********9012", result.accountNumberMasked());
    assertEquals("KB국민은행", result.bankName());
  }

  @Test
  @DisplayName("사업자번호의 구분자를 제거해 실명조회한다")
  void normalizesBusinessRegistrationNumber() {
    when(verificationPort.verify(any()))
        .thenReturn(new AccountRealNameVerificationResult(
            "transaction-id",
            "088",
            "신한은행",
            "1234567890",
            "위하다",
            "1",
            Instant.parse("2026-09-11T01:00:00Z")));

    service.register(new RegisterSellerSettlementAccountCommand(
        UUID.randomUUID(),
        "088",
        "1234567890",
        "위하다",
        AccountHolderType.BUSINESS,
        null,
        "123-45-67890"));

    ArgumentCaptor<AccountRealNameVerificationRequest> requestCaptor =
        ArgumentCaptor.forClass(AccountRealNameVerificationRequest.class);
    verify(verificationPort).verify(requestCaptor.capture());
    assertEquals("1234567890", requestCaptor.getValue().accountHolderInfo());
  }

  @Test
  @DisplayName("실명조회 실패 시 계좌를 저장하지 않는다")
  void doesNotSaveWhenVerificationFails() {
    when(verificationPort.verify(any()))
        .thenThrow(new AccountRealNameVerificationException(
            AccountRealNameVerificationException.Type.HOLDER_MISMATCH));

    BaseException exception = assertThrows(
        BaseException.class,
        () -> service.register(new RegisterSellerSettlementAccountCommand(
            UUID.randomUUID(),
            "004",
            "123456789012",
            "홍길동",
            AccountHolderType.PERSONAL,
            LocalDate.of(1990, 1, 2),
            null)));

    assertEquals(AccountErrorCode.ACCOUNT_HOLDER_MISMATCH, exception.getErrorCode());
    verify(persistencePort, never()).save(any());
  }

  @Test
  @DisplayName("지원하지 않는 금융기관 코드를 거부한다")
  void rejectsUnsupportedBank() {
    BaseException exception = assertThrows(
        BaseException.class,
        () -> service.register(new RegisterSellerSettlementAccountCommand(
            UUID.randomUUID(),
            "999",
            "123456789012",
            "홍길동",
            AccountHolderType.PERSONAL,
            LocalDate.of(1990, 1, 2),
            null)));

    assertEquals(AccountErrorCode.SETTLEMENT_BANK_UNSUPPORTED, exception.getErrorCode());
    verify(verificationPort, never()).verify(any());
  }

  @Test
  @DisplayName("정산계좌 조회 시 복호화한 예금주와 마스킹 계좌번호를 반환한다")
  void getsSettlementAccount() {
    UUID storeId = UUID.randomUUID();
    SellerSettlementAccount account = SellerSettlementAccount.create(
        storeId,
        "090",
        "encrypted:1234567890123",
        "encrypted:홍길동",
        AccountHolderType.PERSONAL,
        "transaction-id",
        Instant.parse("2026-09-11T01:00:00Z"));
    when(persistencePort.findByStoreId(storeId)).thenReturn(Optional.of(account));

    SellerSettlementAccountResult result = service.get(storeId);

    assertEquals("카카오뱅크", result.bankName());
    assertEquals("*********0123", result.accountNumberMasked());
    assertEquals("홍길동", result.accountHolderName());
  }

  private static final class TestSensitiveDataCipher implements SensitiveDataCipher {

    @Override
    public String encrypt(String plaintext) {
      return "encrypted:" + plaintext;
    }

    @Override
    public String decrypt(String encryptedValue) {
      return encryptedValue.substring("encrypted:".length());
    }
  }
}
