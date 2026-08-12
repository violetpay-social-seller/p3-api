package io.point3.p3api.chat.application.send;

import io.point3.p3api.chat.application.port.ChatMessagePort;
import io.point3.p3api.chat.domain.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SendChatMessage {

  private final ChatMessagePort chatMessagePort;

  @Transactional
  public ChatMessage execute(SendChatMessageCommand command) {
    ChatMessage chatMessage =
        ChatMessage.create(command.inquiryId(), command.senderUserId(), command.content());

    return chatMessagePort.save(chatMessage);
  }
}
