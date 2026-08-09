package io.point3.p3api.auth.infrastructure.security;

import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.user.application.render.UserRender;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserRender {

  private final UserRender userRender;

  public CurrentUser read(Authentication authentication) {
    if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
      throw new BaseException(CommonErrorCode.UNAUTHORIZED);
    }

    String cognitoSub = jwt.getSubject();

    return userRender
        .findByCognitoSub(cognitoSub)
        .map(CurrentUser::from)
        .orElseThrow(() -> new BaseException(CommonErrorCode.UNAUTHORIZED));
  }
}
