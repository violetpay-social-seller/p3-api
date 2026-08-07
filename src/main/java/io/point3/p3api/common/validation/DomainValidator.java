package io.point3.p3api.common.validation;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DomainValidator {

    public static String requireText(String value, String fileName) {
        if (value == null || value.isBlank()) {
            throw new BaseException(CommonErrorCode.INVALID_INPUT, fileName + " must not be null");
        }
        return value;
    }
}
