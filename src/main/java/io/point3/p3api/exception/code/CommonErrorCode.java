package io.point3.p3api.exception.code;

import io.point3.p3api.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CommonErrorCode implements ErrorCode {
  INVALID_INPUT(
      "COMMON_INVALID_INPUT_400",
      "Invalid input",
      HttpStatus.BAD_REQUEST,
      "/errors/common/invalid-input"),
  INVALID_ID(
      "COMMON_INVALID_ID_400", "Invalid id", HttpStatus.BAD_REQUEST, "/errors/common/invalid-id"),
  UNAUTHORIZED(
      "COMMON_UNAUTHORIZED_401",
      "UNAUTHORIZED",
      HttpStatus.UNAUTHORIZED,
      "/errors/common/unauthorized"),
  DATA_ACCESS_ERROR(
      "COMMON_DATA_ACCESS_ERROR_500",
      "Data access error",
      HttpStatus.INTERNAL_SERVER_ERROR,
      "/errors/common/data-access-error"),
  INTERNAL_SERVER_ERROR(
      "COMMON_INTERNAL_SERVER_ERROR_500",
      "Internal server error",
      HttpStatus.INTERNAL_SERVER_ERROR,
      "/errors/common/internal-server-error");

  private final String code;
  private final String title;
  private final HttpStatus status;
  private final String type;

  CommonErrorCode(String code, String title, HttpStatus status, String type) {
    this.code = code;
    this.title = title;
    this.status = status;
    this.type = type;
  }
}
