package io.point3.p3api.exception.code;

import io.point3.p3api.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum OrderErrorCode implements ErrorCode {
  ORDER_NOT_FOUND(
      "ORDER_NOT_FOUND_404", "Order not found", HttpStatus.NOT_FOUND, "/errors/order/not-found");

  private final String code;
  private final String title;
  private final HttpStatus status;
  private final String type;

  OrderErrorCode(String code, String title, HttpStatus status, String type) {
    this.code = code;
    this.title = title;
    this.status = status;
    this.type = type;
  }
}
