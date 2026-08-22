package io.point3.p3api.chat.infrastructure.stomp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.point3.p3api.auth.infrastructure.stomp.StompCurrentUserContext;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.chat.controller.ChatStompDestination;
import io.point3.p3api.user.domain.type.UserRole;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.Authentication;

class StompChatMessageAuthorizationInterceptorTest {

  private final ChatStompParticipantAuthorizationService participantAuthorizationService = mock(
      ChatStompParticipantAuthorizationService.class);
  private final StompChatMessageAuthorizationInterceptor interceptor =
      new StompChatMessageAuthorizationInterceptor(participantAuthorizationService);

  @Test
  @DisplayName("참여자 검증이 성공하면 검증된 CurrentUser를 내부 메시지 헤더에 전달한다")
  void addsVerifiedCurrentUserToMessageHeader() {
    UUID inquiryId = UUID.randomUUID();
    Authentication authentication = mock(Authentication.class);
    CurrentUser currentUser = new CurrentUser(UUID.randomUUID(), "구매자", UserRole.BUYER);
    when(participantAuthorizationService.requireParticipant(authentication, inquiryId)).thenReturn(
        currentUser);

    Message<?> result = interceptor.preSend(sendMessage(inquiryId, authentication), mock(
        MessageChannel.class));

    assertEquals(currentUser, result.getHeaders().get(StompCurrentUserContext.HEADER));
    verify(participantAuthorizationService).requireParticipant(authentication, inquiryId);
  }

  private Message<byte[]> sendMessage(UUID inquiryId, Authentication authentication) {
    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
    accessor.setDestination(ChatStompDestination.sendDestination(inquiryId));
    accessor.setUser(authentication);
    return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
  }
}
