package io.point3.p3api.chat.infrastructure.stomp;

import java.security.Principal;
import java.util.UUID;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/** 문의방 토픽을 구독하는 STOMP 세션의 참여 권한을 검증 */
@Component
@RequiredArgsConstructor
public class StompChatSubscriptionAuthorizationInterceptor implements ChannelInterceptor {

  private static final String CHAT_TOPIC_PREFIX = "/topic/inquiries/";

  private final ChatStompParticipantAuthorizationService participantAuthorizationService;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor == null || !StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
      return message;
    }

    String destination = accessor.getDestination();
    if (destination == null || !destination.startsWith(CHAT_TOPIC_PREFIX)) {
      return message;
    }

    try {
      UUID inquiryId = UUID.fromString(destination.substring(CHAT_TOPIC_PREFIX.length()));
      Principal principal = accessor.getUser();
      if (!(principal instanceof Authentication authentication)) {
        throw new BaseException(CommonErrorCode.UNAUTHORIZED);
      }

      participantAuthorizationService.requireParticipant(authentication, inquiryId);
      return message;
    } catch (RuntimeException e) {
      throw new MessageDeliveryException(
          message, "STOMP chat subscription requires an inquiry participant", e);
    }
  }
}
