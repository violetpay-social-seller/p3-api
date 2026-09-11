package io.point3.p3api.common.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.point3.p3api.exception.ErrorCode;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResult(
    String code,
    String type,
    String title,
    int status,
    String instance,
    String detail,
    Map<String, Object> metadata) {

  public ErrorResult {
    metadata = metadata == null || metadata.isEmpty() ? null : Map.copyOf(metadata);
  }

  public static ErrorResult of(ErrorCode errorCode, String instance) {
    return of(errorCode, instance, null, null);
  }

  public static ErrorResult of(
      ErrorCode errorCode, String instance, String detail, Map<String, Object> metadata) {
    return new ErrorResult(
        errorCode.getCode(),
        errorCode.getType(),
        errorCode.getTitle(),
        errorCode.getStatus().value(),
        instance,
        detail,
        metadata);
  }

  @Override
  public Map<String, Object> metadata() {
    return metadata == null ? null : Map.copyOf(metadata);
  }
}
