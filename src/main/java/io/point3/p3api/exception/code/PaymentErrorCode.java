package io.point3.p3api.exception.code;

import io.point3.p3api.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum PaymentErrorCode implements ErrorCode {
  PAYMENT_ATTEMPT_NOT_FOUND(
      "PAYMENT_ATTEMPT_NOT_FOUND_404",
      "Payment attempt not found",
      HttpStatus.NOT_FOUND,
      "/errors/payment/attempt-not-found"),
  PAYMENT_CONFIRMATION_NOT_PAYABLE(
      "PAYMENT_CONFIRMATION_NOT_PAYABLE_400",
      "Order confirmation is not payable",
      HttpStatus.BAD_REQUEST,
      "/errors/payment/confirmation-not-payable"),
  PAYMENT_ATTEMPT_FORBIDDEN(
      "PAYMENT_ATTEMPT_FORBIDDEN_403",
      "Payment attempt is forbidden",
      HttpStatus.FORBIDDEN,
      "/errors/payment/attempt-forbidden"),
  PAYMENT_SESSION_MISMATCH(
      "PAYMENT_SESSION_MISMATCH_400",
      "Payment session does not match",
      HttpStatus.BAD_REQUEST,
      "/errors/payment/session-mismatch"),
  PAYMENT_CAPTURE_MESSAGE_INVALID(
      "PAYMENT_CAPTURE_MESSAGE_INVALID_400",
      "Payment capture message is invalid",
      HttpStatus.BAD_REQUEST,
      "/errors/payment/capture-message-invalid"),
  PAYMENT_EXTERNAL_UNAVAILABLE(
      "PAYMENT_EXTERNAL_UNAVAILABLE_502",
      "Payment provider is unavailable",
      HttpStatus.BAD_GATEWAY,
      "/errors/payment/external-unavailable");

  private final String code;
  private final String title;
  private final HttpStatus status;
  private final String type;

  PaymentErrorCode(String code, String title, HttpStatus status, String type) {
    this.code = code;
    this.title = title;
    this.status = status;
    this.type = type;
  }
}
