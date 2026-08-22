package io.point3.p3api.auth.infrastructure.stomp;

import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import org.springframework.core.MethodParameter;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.stereotype.Component;

@Component
public class StompCurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(StompCurrentUser.class)
        && parameter.getParameterType().equals(CurrentUser.class);
  }

  @Override
  public Object resolveArgument(MethodParameter parameter, Message<?> message) {
    Object currentUser = message.getHeaders().get(StompCurrentUserContext.HEADER);
    if (!(currentUser instanceof CurrentUser)) {
      throw new BaseException(CommonErrorCode.UNAUTHORIZED);
    }

    return currentUser;
  }
}
