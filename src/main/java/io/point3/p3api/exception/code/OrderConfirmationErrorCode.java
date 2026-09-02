package io.point3.p3api.exception.code;

import io.point3.p3api.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum OrderConfirmationErrorCode implements ErrorCode {
  ORDER_CONFIRMATION_NOT_FOUND(
      "ORDER_CONFIRMATION_NOT_FOUND_404",
      "Order confirmation not found",
      HttpStatus.NOT_FOUND,
      "/errors/order-confirmation/not-found"),
  ORDER_CONFIRMATION_SUBMISSION_INVALID(
      "ORDER_CONFIRMATION_SUBMISSION_INVALID_400",
      "Order form submission is invalid for this confirmation",
      HttpStatus.BAD_REQUEST,
      "/errors/order-confirmation/submission-invalid"),
  ORDER_CONFIRMATION_STATUS_FORBIDDEN(
      "ORDER_CONFIRMATION_STATUS_FORBIDDEN_400",
      "Order confirmation status can not be changed",
      HttpStatus.BAD_REQUEST,
      "/errors/order-confirmation/status-forbidden"),
  ORDER_CONFIRMATION_AMOUNT_INVALID(
      "ORDER_CONFIRMATION_AMOUNT_INVALID_400",
      "Order confirmation amount is invalid",
      HttpStatus.BAD_REQUEST,
      "/errors/order-confirmation/amount-invalid"),
  ORDER_CONFIRMATION_AMOUNT_UNCONFIRMED(
      "ORDER_CONFIRMATION_AMOUNT_UNCONFIRMED_400",
      "Order confirmation amount requires seller confirmation",
      HttpStatus.BAD_REQUEST,
      "/errors/order-confirmation/amount-unconfirmed");

  private final String code;
  private final String title;
  private final HttpStatus status;
  private final String type;

  OrderConfirmationErrorCode(String code, String title, HttpStatus status, String type) {
    this.code = code;
    this.title = title;
    this.status = status;
    this.type = type;
  }
}
