package io.point3.p3api.exception.code;

import io.point3.p3api.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ChatErrorCode implements ErrorCode {
  CHAT_INQUIRY_NOT_FOUND(
      "CHAT_INQUIRY_NOT_FOUND_404",
      "Chat inquiry not found",
      HttpStatus.NOT_FOUND,
      "/errors/chat/inquiry-not-found"),
  CHAT_PARTICIPANT_FORBIDDEN(
      "CHAT_PARTICIPANT_FORBIDDEN_403",
      "Chat room participant required",
      HttpStatus.FORBIDDEN,
      "/errors/chat/participant-forbidden");

  private final String code;
  private final String title;
  private final HttpStatus status;
  private final String type;

  ChatErrorCode(String code, String title, HttpStatus status, String type) {
    this.code = code;
    this.title = title;
    this.status = status;
    this.type = type;
  }
}
