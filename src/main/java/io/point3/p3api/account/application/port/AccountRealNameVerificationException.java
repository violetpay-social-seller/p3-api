package io.point3.p3api.account.application.port;

import lombok.Getter;

@Getter
public class AccountRealNameVerificationException extends RuntimeException {

  private final Type type;
  private final AccountVerificationProviderError providerError;

  public AccountRealNameVerificationException(Type type) {
    this(type, null, null);
  }

  public AccountRealNameVerificationException(Type type, Throwable cause) {
    this(type, null, cause);
  }

  public AccountRealNameVerificationException(Type type, String providerCode) {
    this(type, new AccountVerificationProviderError(null, providerCode, null, null, null), null);
  }

  public AccountRealNameVerificationException(
      Type type, AccountVerificationProviderError providerError) {
    this(type, providerError, null);
  }

  public AccountRealNameVerificationException(
      Type type, AccountVerificationProviderError providerError, Throwable cause) {
    super(type.name(), cause);
    this.type = type;
    this.providerError = providerError;
  }

  public String getProviderCode() {
    return providerError == null ? null : providerError.primaryCode();
  }

  public enum Type {
    CONFIGURATION,
    AUTHENTICATION,
    REJECTED,
    HOLDER_MISMATCH,
    INVALID_RESPONSE,
    UNAVAILABLE
  }
}
