package io.point3.p3api.account.application.port;

import java.util.Objects;
import java.util.Set;

public record AccountRealNameVerificationRequest(
    String bankCode,
    String accountNumber,
    String accountHolderName,
    String accountHolderInfoType,
    String accountHolderInfo) {

  private static final Set<String> SUPPORTED_ACCOUNT_HOLDER_INFO_TYPES =
      Set.of(" ", "1", "2", "3", "4", "5", "6", "E", "N");

  public AccountRealNameVerificationRequest {
    bankCode = requirePattern(bankCode, "[0-9]{3}", "bankCode");
    accountNumber = requirePattern(accountNumber, "[0-9]{1,16}", "accountNumber");
    accountHolderName = requireText(accountHolderName, "accountHolderName");
    accountHolderInfoType = Objects.requireNonNull(accountHolderInfoType, "accountHolderInfoType");
    if (!SUPPORTED_ACCOUNT_HOLDER_INFO_TYPES.contains(accountHolderInfoType)) {
      throw new IllegalArgumentException("accountHolderInfoType is not supported");
    }
    accountHolderInfo = requirePattern(accountHolderInfo, "[A-Za-z0-9]{1,13}", "accountHolderInfo");
  }

  private static String requirePattern(String value, String pattern, String fieldName) {
    String text = requireText(value, fieldName);
    if (!text.matches(pattern)) {
      throw new IllegalArgumentException(fieldName + " has invalid format");
    }
    return text;
  }

  private static String requireText(String value, String fieldName) {
    Objects.requireNonNull(value, fieldName);
    if (value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
    return value;
  }
}
