package io.point3.p3api.exception.code;

import io.point3.p3api.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ProductErrorCode implements ErrorCode {
  PRODUCT_NOT_FOUND(
      "PRODUCT_NOT_FOUND_404",
      "Product not found",
      HttpStatus.NOT_FOUND,
      "/errors/product/not-found"),
  PRODUCT_STATUS_FORBIDDEN(
      "PRODUCT_STATUS_FORBIDDEN_400",
      "Product status can not be changed",
      HttpStatus.BAD_REQUEST,
      "/errors/product/status-forbidden"),
  PRODUCT_OPTION_INVALID(
      "PRODUCT_OPTION_INVALID_400",
      "Product option selection is invalid",
      HttpStatus.BAD_REQUEST,
      "/errors/product/option-invalid");

  private final String code;
  private final String title;
  private final HttpStatus status;
  private final String type;

  ProductErrorCode(String code, String title, HttpStatus status, String type) {
    this.code = code;
    this.title = title;
    this.status = status;
    this.type = type;
  }
}
