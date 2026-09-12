package io.point3.p3api.account.infrastructure.external.temporary;

import io.point3.p3api.account.application.port.AccountRealNameVerificationPort;
import io.point3.p3api.account.application.port.AccountRealNameVerificationRequest;
import io.point3.p3api.account.application.port.AccountRealNameVerificationResult;
import java.time.Clock;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "p3.kftc", name = "verification-enabled", havingValue = "false")
@RequiredArgsConstructor
@Slf4j
public class TemporaryAccountRealNameVerificationAdapter
    implements AccountRealNameVerificationPort {

  private static final String TEMPORARY_TRANSACTION_PREFIX = "TEMP";
  private static final String TEMPORARY_BANK_NAME = "TEMPORARY_UNVERIFIED";
  private static final String TEMPORARY_ACCOUNT_TYPE = "TEMPORARY";

  private final Clock clock;

  @Override
  public AccountRealNameVerificationResult verify(AccountRealNameVerificationRequest request) {
    log.warn("Temporary account real-name verification bypass is enabled");
    return new AccountRealNameVerificationResult(
        temporaryTransactionId(),
        request.bankCode(),
        TEMPORARY_BANK_NAME,
        request.accountNumber(),
        request.accountHolderName(),
        TEMPORARY_ACCOUNT_TYPE,
        clock.instant());
  }

  private String temporaryTransactionId() {
    return TEMPORARY_TRANSACTION_PREFIX + UUID.randomUUID().toString().replace("-", "");
  }
}
