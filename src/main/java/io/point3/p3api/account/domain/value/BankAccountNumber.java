package io.point3.p3api.account.domain.value;

import java.util.Objects;

public final class BankAccountNumber {

  private static final int MAX_LENGTH = 16;
  private static final int VISIBLE_SUFFIX_LENGTH = 4;

  private final String value;

  private BankAccountNumber(String value) {
    this.value = normalize(value);
    validate(this.value);
  }

  public static BankAccountNumber of(String value) {
    return new BankAccountNumber(value);
  }

  public String value() {
    return value;
  }

  public String masked() {
    int visibleLength = Math.min(VISIBLE_SUFFIX_LENGTH, value.length());
    int maskedLength = value.length() - visibleLength;
    return "*".repeat(maskedLength) + value.substring(maskedLength);
  }

  @Override
  public String toString() {
    return "BankAccountNumber[masked=" + masked() + "]";
  }

  private static String normalize(String value) {
    Objects.requireNonNull(value, "value");
    return value.replace("-", "").replaceAll("\\s+", "");
  }

  private static void validate(String value) {
    if (value.isEmpty() || value.length() > MAX_LENGTH || !value.matches("[0-9]+")) {
      throw new IllegalArgumentException("Bank account number must contain up to 16 digits");
    }
  }
}
