package io.point3.p3api.chat.controller;

import io.point3.p3api.auth.infrastructure.stomp.StompCurrentUser;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.chat.application.port.ChatMessageRealtimePublisherPort;
import io.point3.p3api.chat.application.send.SendChatMessageCommand;
import io.point3.p3api.chat.application.send.SendChatMessageResult;
import io.point3.p3api.chat.application.send.SendChatMessageUseCase;
import io.point3.p3api.chat.controller.request.SendChatMessageStompRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

/** STOMP 채팅 메시지를 저장하고 문의방 구독자에게 실시간으로 발행한다. */
@Controller
@RequiredArgsConstructor
public class ChatStompController {

  private final SendChatMessageUseCase sendChatMessageUseCase;
  private final ChatMessageRealtimePublisherPort chatMessageRealtimePublisherPort;

  @MessageMapping(ChatStompDestination.MESSAGE_MAPPING)
  public void sendMessage(
      @DestinationVariable UUID inquiryId,
      @Valid @Payload SendChatMessageStompRequest request,
      @StompCurrentUser CurrentUser currentUser) {
    SendChatMessageResult result = sendChatMessageUseCase.execute(
        SendChatMessageCommand.of(inquiryId, currentUser.userId(), request.content()));

    chatMessageRealtimePublisherPort.publish(result);
  }
}
