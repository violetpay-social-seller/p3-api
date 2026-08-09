package io.point3.p3api.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
  private final ErrorCode errorCode;

  public BaseException(ErrorCode errorCode) {
    super(errorCode.getTitle());
    this.errorCode = errorCode;
  }

  public BaseException(ErrorCode errorCode, String detail) {
    super(detail);
    this.errorCode = errorCode;
  }
}
