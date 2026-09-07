package io.point3.p3api.inquiry.infrastructure.stomp;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.point3.p3api.auth.infrastructure.security.CurrentUserRender;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.inquiry.controller.InquiryListStompDestination;
import io.point3.p3api.user.domain.type.UserRole;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.Authentication;

class StompInquiryListSubscriptionAuthorizationInterceptorTest {

  private final CurrentUserRender currentUserRender = mock(CurrentUserRender.class);
  private final StompInquiryListSubscriptionAuthorizationInterceptor interceptor =
      new StompInquiryListSubscriptionAuthorizationInterceptor(currentUserRender);

  @Test
  @DisplayName("사용자 문의 목록 토픽은 본인만 구독할 수 있다")
  void allowsCurrentUserTopicSubscription() {
    UUID userId = UUID.randomUUID();
    Authentication authentication = mock(Authentication.class);
    when(currentUserRender.read(authentication))
        .thenReturn(new CurrentUser(userId, "구매자", UserRole.BUYER));
    Message<byte[]> message = subscribeMessage(userId, authentication);

    Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));

    assertSame(message, result);
    verify(currentUserRender).read(authentication);
  }

  @Test
  @DisplayName("다른 사용자의 문의 목록 토픽 구독은 거절한다")
  void rejectsOtherUserTopicSubscription() {
    UUID userId = UUID.randomUUID();
    Authentication authentication = mock(Authentication.class);
    when(currentUserRender.read(authentication))
        .thenReturn(new CurrentUser(UUID.randomUUID(), "구매자", UserRole.BUYER));
    Message<byte[]> message = subscribeMessage(userId, authentication);

    assertThrows(
        MessageDeliveryException.class,
        () -> interceptor.preSend(message, mock(MessageChannel.class)));
  }

  private Message<byte[]> subscribeMessage(UUID userId, Authentication authentication) {
    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
    accessor.setDestination(InquiryListStompDestination.topicDestination(userId));
    accessor.setUser(authentication);
    return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
  }
}
