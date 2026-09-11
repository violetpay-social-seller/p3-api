package io.point3.p3api.exception;

import java.util.Map;
import lombok.Getter;

@Getter
public class DetailedBaseException extends BaseException {

  private final String detail;
  private final Map<String, Object> metadata;

  public DetailedBaseException(ErrorCode errorCode, String detail, Map<String, Object> metadata) {
    super(errorCode, detail);
    this.detail = normalize(detail);
    this.metadata = metadata == null || metadata.isEmpty() ? null : Map.copyOf(metadata);
  }

  private String normalize(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}
