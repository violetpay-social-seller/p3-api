package io.point3.p3api.chat.infrastructure.stomp;

import io.point3.p3api.auth.infrastructure.stomp.StompCurrentUserContext;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/** 문의방에 메시지를 보내는 STOMP 세션의 참여 권한을 검증한다. */
@Component
@RequiredArgsConstructor
public class StompChatMessageAuthorizationInterceptor implements ChannelInterceptor {

  private static final String CHAT_MESSAGE_PREFIX = "/app/inquiries/";
  private static final String MESSAGES_SUFFIX = "/messages";

  private final ChatStompParticipantAuthorizationService participantAuthorizationService;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

    if (accessor == null || !StompCommand.SEND.equals(accessor.getCommand())) {
      return message;
    }

    String destination = accessor.getDestination();
    try {
      validateSendDestination(destination);
      UUID inquiryId = extractInquiryId(destination);
      Principal principal = accessor.getUser();
      if (!(principal instanceof Authentication authentication)) {
        throw new BaseException(CommonErrorCode.UNAUTHORIZED);
      }

      CurrentUser currentUser =
          participantAuthorizationService.requireParticipant(authentication, inquiryId);
      accessor.setHeader(StompCurrentUserContext.HEADER, currentUser);
      return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
    } catch (RuntimeException e) {
      throw new MessageDeliveryException(
          message, "STOMP chat message requires an inquiry participant", e);
    }
  }

  private void validateSendDestination(String destination) {
    if (destination == null || !destination.startsWith(CHAT_MESSAGE_PREFIX)) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT);
    }
  }

  private UUID extractInquiryId(String destination) {
    if (!destination.endsWith(MESSAGES_SUFFIX)) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT);
    }

    String inquiryId = destination.substring(
        CHAT_MESSAGE_PREFIX.length(), destination.length() - MESSAGES_SUFFIX.length());
    try {
      return UUID.fromString(inquiryId);
    } catch (IllegalArgumentException e) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT);
    }
  }
}
