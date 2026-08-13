package io.point3.p3api.exception.code;

import io.point3.p3api.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum StoreErrorCode implements ErrorCode {
  STORE_ALREADY_EXISTS(
      "STORE_ALREADY_EXISTS_409",
      "Store already exists",
      HttpStatus.CONFLICT,
      "/errors/store/already-exists"),
  STORE_NOT_FOUND(
      "STORE_NOT_FOUND_404", "Store not found", HttpStatus.NOT_FOUND, "/errors/store/not-found"),
  STORE_STATUS_FORBIDDEN(
      "STORE_STATUS_FORBIDDEN_400",
      "Store status can not be changed",
      HttpStatus.BAD_REQUEST,
      "/errors/store/status-forbidden");

  private final String code;
  private final String title;
  private final HttpStatus status;
  private final String type;

  StoreErrorCode(String code, String title, HttpStatus status, String type) {
    this.code = code;
    this.title = title;
    this.status = status;
    this.type = type;
  }
}
