package io.point3.p3api.account.application.port;

import lombok.Getter;

@Getter
public class AccountRealNameVerificationException extends RuntimeException {

  private final Type type;
  private final String providerCode;

  public AccountRealNameVerificationException(Type type) {
    this(type, null, null);
  }

  public AccountRealNameVerificationException(Type type, Throwable cause) {
    this(type, null, cause);
  }

  public AccountRealNameVerificationException(Type type, String providerCode) {
    this(type, providerCode, null);
  }

  private AccountRealNameVerificationException(Type type, String providerCode, Throwable cause) {
    super(type.name(), cause);
    this.type = type;
    this.providerCode = providerCode;
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
