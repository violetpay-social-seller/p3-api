package io.point3.p3api.account.application.settlement;

import io.point3.p3api.account.application.port.AccountRealNameVerificationException;
import io.point3.p3api.account.application.port.AccountRealNameVerificationPort;
import io.point3.p3api.account.application.port.AccountRealNameVerificationRequest;
import io.point3.p3api.account.application.port.AccountRealNameVerificationResult;
import io.point3.p3api.account.application.port.SensitiveDataCipher;
import io.point3.p3api.account.application.settlement.port.SellerSettlementAccountPersistencePort;
import io.point3.p3api.account.domain.entity.SellerSettlementAccount;
import io.point3.p3api.account.domain.type.AccountHolderType;
import io.point3.p3api.account.domain.type.SettlementBank;
import io.point3.p3api.account.domain.value.BankAccountNumber;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.AccountErrorCode;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerSettlementAccountService
    implements SellerSettlementAccountRegisterUseCase,
        SellerSettlementAccountQueryUseCase,
        SettlementBankQueryUseCase {

  private static final String PERSONAL_HOLDER_INFO_TYPE = "1";
  private static final String BUSINESS_HOLDER_INFO_TYPE = "2";
  private static final DateTimeFormatter BIRTH_DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd");

  private final SellerSettlementAccountPersistencePort persistencePort;
  private final AccountRealNameVerificationPort verificationPort;
  private final SensitiveDataCipher sensitiveDataCipher;

  @Override
  public SellerSettlementAccountResult register(RegisterSellerSettlementAccountCommand command) {
    SettlementBank bank = findBank(command.bankCode());
    BankAccountNumber accountNumber = accountNumber(command.accountNumber());
    String accountHolderName = requireText(command.accountHolderName());
    String holderInfo = holderInfo(command);

    AccountRealNameVerificationResult verificationResult;
    try {
      verificationResult = verificationPort.verify(new AccountRealNameVerificationRequest(
          bank.code(),
          accountNumber.value(),
          accountHolderName,
          holderInfoType(command.holderType()),
          holderInfo));
    } catch (AccountRealNameVerificationException exception) {
      throw toBaseException(exception);
    }

    SellerSettlementAccount account = persistencePort
        .findByStoreId(command.storeId())
        .map(existing -> replace(existing, command.holderType(), verificationResult))
        .orElseGet(() -> create(command.storeId(), command.holderType(), verificationResult));

    return toResult(persistencePort.save(account));
  }

  @Override
  public SellerSettlementAccountResult get(UUID storeId) {
    SellerSettlementAccount account = persistencePort
        .findByStoreId(storeId)
        .orElseThrow(() -> new BaseException(AccountErrorCode.SETTLEMENT_ACCOUNT_NOT_FOUND));
    return toResult(account);
  }

  @Override
  public List<SettlementBankResult> getBanks() {
    return Arrays.stream(SettlementBank.values())
        .map(SettlementBankResult::from)
        .toList();
  }

  private SellerSettlementAccount create(
      UUID storeId,
      AccountHolderType holderType,
      AccountRealNameVerificationResult verificationResult) {
    return SellerSettlementAccount.create(
        storeId,
        verificationResult.bankCode(),
        sensitiveDataCipher.encrypt(verificationResult.accountNumber()),
        sensitiveDataCipher.encrypt(verificationResult.accountHolderName()),
        holderType,
        verificationResult.providerTransactionId(),
        verificationResult.verifiedAt());
  }

  private SellerSettlementAccount replace(
      SellerSettlementAccount account,
      AccountHolderType holderType,
      AccountRealNameVerificationResult verificationResult) {
    account.replaceVerifiedAccount(
        verificationResult.bankCode(),
        sensitiveDataCipher.encrypt(verificationResult.accountNumber()),
        sensitiveDataCipher.encrypt(verificationResult.accountHolderName()),
        holderType,
        verificationResult.providerTransactionId(),
        verificationResult.verifiedAt());
    return account;
  }

  private SellerSettlementAccountResult toResult(SellerSettlementAccount account) {
    SettlementBank bank = findBank(account.getBankCode());
    BankAccountNumber accountNumber =
        accountNumber(sensitiveDataCipher.decrypt(account.getEncryptedAccountNumber()));
    return new SellerSettlementAccountResult(
        bank.code(),
        bank.displayName(),
        accountNumber.masked(),
        sensitiveDataCipher.decrypt(account.getEncryptedAccountHolderName()),
        account.getAccountHolderType(),
        account.getVerifiedAt());
  }

  private SettlementBank findBank(String bankCode) {
    return SettlementBank.findByCode(bankCode)
        .orElseThrow(() -> new BaseException(AccountErrorCode.SETTLEMENT_BANK_UNSUPPORTED));
  }

  private BankAccountNumber accountNumber(String value) {
    if (value == null) {
      throw new BaseException(AccountErrorCode.SETTLEMENT_ACCOUNT_INPUT_INVALID);
    }
    try {
      return BankAccountNumber.of(value);
    } catch (IllegalArgumentException exception) {
      throw new BaseException(AccountErrorCode.SETTLEMENT_ACCOUNT_INPUT_INVALID);
    }
  }

  private String holderInfo(RegisterSellerSettlementAccountCommand command) {
    if (command.holderType() == AccountHolderType.PERSONAL
        && command.birthDate() != null
        && command.businessRegistrationNumber() == null) {
      return command.birthDate().format(BIRTH_DATE_FORMAT);
    }
    if (command.holderType() == AccountHolderType.BUSINESS
        && command.birthDate() == null
        && command.businessRegistrationNumber() != null) {
      String number = command.businessRegistrationNumber().replace("-", "").replaceAll("\\s+", "");
      if (number.matches("[0-9]{10}")) {
        return number;
      }
    }
    throw new BaseException(AccountErrorCode.SETTLEMENT_ACCOUNT_INPUT_INVALID);
  }

  private String holderInfoType(AccountHolderType holderType) {
    return switch (holderType) {
      case PERSONAL -> PERSONAL_HOLDER_INFO_TYPE;
      case BUSINESS -> BUSINESS_HOLDER_INFO_TYPE;
    };
  }

  private String requireText(String value) {
    if (value == null || value.isBlank()) {
      throw new BaseException(AccountErrorCode.SETTLEMENT_ACCOUNT_INPUT_INVALID);
    }
    return value.trim();
  }

  private BaseException toBaseException(AccountRealNameVerificationException exception) {
    return switch (exception.getType()) {
      case HOLDER_MISMATCH -> new BaseException(AccountErrorCode.ACCOUNT_HOLDER_MISMATCH);
      case REJECTED -> new BaseException(AccountErrorCode.ACCOUNT_VERIFICATION_REJECTED);
      case CONFIGURATION ->
        new BaseException(AccountErrorCode.ACCOUNT_VERIFICATION_CONFIGURATION_INVALID);
      case AUTHENTICATION, INVALID_RESPONSE, UNAVAILABLE ->
        new BaseException(AccountErrorCode.ACCOUNT_VERIFICATION_UNAVAILABLE);
    };
  }
}
