package io.point3.p3api.common.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResult(String code, String type, String title, int status, String instance) {
    public static ErrorResult of(ErrorCode errorCode, String instance) {
        return new ErrorResult(
                errorCode.getCode(),
                errorCode.getType(),
                errorCode.getTitle(),
                errorCode.getStatus().value(),
                instance);
    }
}
