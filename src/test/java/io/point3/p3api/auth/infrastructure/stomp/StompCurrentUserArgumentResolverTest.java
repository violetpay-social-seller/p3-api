package io.point3.p3api.auth.infrastructure.stomp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.user.domain.type.UserRole;
import java.lang.reflect.Method;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

class StompCurrentUserArgumentResolverTest {

  private final StompCurrentUserArgumentResolver resolver = new StompCurrentUserArgumentResolver();

  @Test
  @DisplayName("내부 메시지 헤더의 CurrentUser를 MessageMapping 파라미터로 주입한다")
  void resolvesCurrentUserFromMessageHeader() throws Exception {
    CurrentUser currentUser = new CurrentUser(UUID.randomUUID(), "구매자", UserRole.BUYER);
    MethodParameter parameter = parameter();
    Message<String> message = MessageBuilder.withPayload("message")
        .setHeader(StompCurrentUserContext.HEADER, currentUser)
        .build();

    assertEquals(currentUser, resolver.resolveArgument(parameter, message));
  }

  private MethodParameter parameter() throws NoSuchMethodException {
    Method method = TestMessageHandler.class.getDeclaredMethod("handle", CurrentUser.class);
    return new MethodParameter(method, 0);
  }

  private static class TestMessageHandler {
    void handle(@StompCurrentUser CurrentUser currentUser) {}
  }
}
