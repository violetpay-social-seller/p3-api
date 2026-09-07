package io.point3.p3api.inquiry.infrastructure.stomp;

import io.point3.p3api.auth.infrastructure.security.CurrentUserRender;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.inquiry.controller.InquiryListStompDestination;
import java.security.Principal;
import java.util.Optional;
import java.util.UUID;
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

/** 문의 목록 사용자 토픽은 본인만 구독 가능 */
@Component
@RequiredArgsConstructor
public class StompInquiryListSubscriptionAuthorizationInterceptor implements ChannelInterceptor {

  private final CurrentUserRender currentUserRender;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor == null || !StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
      return message;
    }

    Optional<UUID> userId = InquiryListStompDestination.topicUserId(accessor.getDestination());
    if (userId.isEmpty()) {
      return message;
    }

    return authorize(message, accessor, userId.get());
  }

  private Message<?> authorize(Message<?> message, StompHeaderAccessor accessor, UUID userId) {
    try {
      Principal principal = accessor.getUser();
      if (!(principal instanceof Authentication authentication)) {
        throw new BaseException(CommonErrorCode.UNAUTHORIZED);
      }

      CurrentUser currentUser = currentUserRender.read(authentication);
      if (!currentUser.userId().equals(userId)) {
        throw new BaseException(CommonErrorCode.UNAUTHORIZED);
      }

      return message;
    } catch (RuntimeException e) {
      throw new MessageDeliveryException(
          message, "STOMP inquiry list subscription requires the current user", e);
    }
  }
}
