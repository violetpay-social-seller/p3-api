package io.point3.p3api.exception.code;

import io.point3.p3api.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AccountErrorCode implements ErrorCode {
  SETTLEMENT_ACCOUNT_NOT_FOUND(
      "SETTLEMENT_ACCOUNT_NOT_FOUND_404",
      "Settlement account not found",
      HttpStatus.NOT_FOUND,
      "/errors/account/settlement-account-not-found"),
  SETTLEMENT_BANK_UNSUPPORTED(
      "SETTLEMENT_BANK_UNSUPPORTED_400",
      "Settlement bank is not supported",
      HttpStatus.BAD_REQUEST,
      "/errors/account/settlement-bank-unsupported"),
  SETTLEMENT_ACCOUNT_INPUT_INVALID(
      "SETTLEMENT_ACCOUNT_INPUT_INVALID_400",
      "Settlement account input is invalid",
      HttpStatus.BAD_REQUEST,
      "/errors/account/settlement-account-input-invalid"),
  ACCOUNT_HOLDER_MISMATCH(
      "ACCOUNT_HOLDER_MISMATCH_400",
      "Account holder does not match",
      HttpStatus.BAD_REQUEST,
      "/errors/account/account-holder-mismatch"),
  ACCOUNT_VERIFICATION_REJECTED(
      "ACCOUNT_VERIFICATION_REJECTED_400",
      "Account verification was rejected",
      HttpStatus.BAD_REQUEST,
      "/errors/account/account-verification-rejected"),
  ACCOUNT_VERIFICATION_CONFIGURATION_INVALID(
      "ACCOUNT_VERIFICATION_CONFIGURATION_INVALID_500",
      "Account verification configuration is invalid",
      HttpStatus.INTERNAL_SERVER_ERROR,
      "/errors/account/account-verification-configuration-invalid"),
  ACCOUNT_VERIFICATION_UNAVAILABLE(
      "ACCOUNT_VERIFICATION_UNAVAILABLE_502",
      "Account verification is unavailable",
      HttpStatus.BAD_GATEWAY,
      "/errors/account/account-verification-unavailable");

  private final String code;
  private final String title;
  private final HttpStatus status;
  private final String type;

  AccountErrorCode(String code, String title, HttpStatus status, String type) {
    this.code = code;
    this.title = title;
    this.status = status;
    this.type = type;
  }
}
