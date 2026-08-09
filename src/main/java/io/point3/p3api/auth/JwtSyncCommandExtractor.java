package io.point3.p3api.auth;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.user.application.sync.SyncCommand;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class JwtSyncCommandExtractor {

    public SyncCommand extract(Jwt jwt) {
        if (jwt == null) {
            throw new BaseException(CommonErrorCode.UNAUTHORIZED);
        }

        String cognitoSub = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("name");

        validateNotBlank(cognitoSub,"cognitoSub");
        validateNotBlank(email,"email");
        validateNotBlank(name,"name");

        return SyncCommand.of(cognitoSub,email,name);
    }

    private void validateNotBlank(String value, String paramName) {
        if (isBlank(value)) {
            throw new BaseException(CommonErrorCode.UNAUTHORIZED, paramName + " must not be blank");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

}
