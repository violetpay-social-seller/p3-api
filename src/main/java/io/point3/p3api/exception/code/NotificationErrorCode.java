package io.point3.p3api.exception.code;

import io.point3.p3api.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum NotificationErrorCode implements ErrorCode {
  NOTIFICATION_NOT_FOUND(
      "NOTIFICATION_NOT_FOUND_404",
      "Notification not found",
      HttpStatus.NOT_FOUND,
      "/errors/notification/not-found");

  private final String code;
  private final String title;
  private final HttpStatus status;
  private final String type;

  NotificationErrorCode(String code, String title, HttpStatus status, String type) {
    this.code = code;
    this.title = title;
    this.status = status;
    this.type = type;
  }
}
