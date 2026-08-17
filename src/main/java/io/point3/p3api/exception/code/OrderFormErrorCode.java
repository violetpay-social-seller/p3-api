package io.point3.p3api.exception.code;

import io.point3.p3api.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum OrderFormErrorCode implements ErrorCode {
  ORDER_FORM_NOT_FOUND(
      "ORDER_FORM_NOT_FOUND_404",
      "Order form not found",
      HttpStatus.NOT_FOUND,
      "/errors/order-form/not-found"),
  ORDER_FORM_ACTIVE_ALREADY_EXISTS(
      "ORDER_FORM_ACTIVE_ALREADY_EXISTS_409",
      "Active order form already exists",
      HttpStatus.CONFLICT,
      "/errors/order-form/active-already-exists"),
  ORDER_FORM_UNKNOWN_FIELD(
      "ORDER_FORM_UNKNOWN_FIELD_400",
      "Order form contains unknown field",
      HttpStatus.BAD_REQUEST,
      "/errors/order-form/unknown-field"),
  ORDER_FORM_REQUIRED_FIELD_MISSING(
      "ORDER_FORM_REQUIRED_FIELD_MISSING_400",
      "Required order form field is missing",
      HttpStatus.BAD_REQUEST,
      "/errors/order-form/required-field-missing"),
  ORDER_FORM_FIELD_VALUE_INVALID(
      "ORDER_FORM_FIELD_VALUE_INVALID_400",
      "Order form field value is invalid",
      HttpStatus.BAD_REQUEST,
      "/errors/order-form/field-value-invalid"),
  ORDER_FORM_IMAGE_COUNT_EXCEEDED(
      "ORDER_FORM_IMAGE_COUNT_EXCEEDED_400",
      "Order form image count exceeded",
      HttpStatus.BAD_REQUEST,
      "/errors/order-form/image-count-exceeded"),
  ORDER_FORM_NOTICE_AGREEMENT_REQUIRED(
      "ORDER_FORM_NOTICE_AGREEMENT_REQUIRED_400",
      "Order form notice agreement is required",
      HttpStatus.BAD_REQUEST,
      "/errors/order-form/notice-agreement-required");

  private final String code;
  private final String title;
  private final HttpStatus status;
  private final String type;

  OrderFormErrorCode(String code, String title, HttpStatus status, String type) {
    this.code = code;
    this.title = title;
    this.status = status;
    this.type = type;
  }
}
