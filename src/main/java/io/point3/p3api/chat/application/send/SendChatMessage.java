package io.point3.p3api.chat.application.send;

import io.point3.p3api.chat.application.port.ChatEventPort;
import io.point3.p3api.chat.application.port.ChatMessagePort;
import io.point3.p3api.chat.domain.entity.ChatEvent;
import io.point3.p3api.chat.domain.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SendChatMessage {

  private final ChatMessagePort chatMessagePort;
  private final ChatEventPort chatEventPort;

  @Transactional
  public ChatMessage execute(SendChatMessageCommand command) {
    ChatMessage chatMessage =
        ChatMessage.create(command.inquiryId(), command.senderUserId(), command.content());

    ChatMessage savedChatMessage = chatMessagePort.save(chatMessage);

    chatEventPort.save(ChatEvent.message(
        savedChatMessage.getInquiryId(),
        savedChatMessage.getSenderUserId(),
        savedChatMessage.getId()));

    return savedChatMessage;
  }
}
