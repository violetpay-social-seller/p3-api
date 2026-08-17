package io.point3.p3api.chat.infrastructure.websocket;

import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.chat.application.send.SendChatMessageCommand;
import io.point3.p3api.chat.application.send.SendChatMessageResult;
import io.point3.p3api.chat.application.send.SendChatMessageUseCase;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

/** STOMP 채팅 메시지를 저장하고 문의방 구독자에게 실시간으로 발행한다. */
@Controller
@RequiredArgsConstructor
public class ChatStompController {

  private final ChatStompParticipantAuthorizationService participantAuthorizationService;
  private final SendChatMessageUseCase sendChatMessageUseCase;
  private final SimpMessagingTemplate messagingTemplate;

  @MessageMapping(ChatStompDestination.MESSAGE_MAPPING)
  public void sendMessage(
      @DestinationVariable UUID inquiryId,
      @Valid @Payload SendChatMessageStompRequest request,
      Principal principal) {
    if (!(principal instanceof Authentication authentication)) {
      throw new BaseException(CommonErrorCode.UNAUTHORIZED);
    }

    CurrentUser currentUser = participantAuthorizationService.requireParticipant(
        authentication, inquiryId);
    SendChatMessageResult result = sendChatMessageUseCase.execute(
        new SendChatMessageCommand(inquiryId, currentUser.userId(), request.content()));

    messagingTemplate.convertAndSend(
        ChatStompDestination.topicDestination(inquiryId), ChatTimelineItemStompEvent.from(result));
  }
}
