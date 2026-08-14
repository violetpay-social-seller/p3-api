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
public class SendChatMessageService implements SendChatMessageUseCase {

  private final ChatMessagePort chatMessagePort;
  private final ChatEventPort chatEventPort;

  @Override
  @Transactional
  public SendChatMessageResult execute(SendChatMessageCommand command) {
    ChatMessage chatMessage =
        ChatMessage.create(command.inquiryId(), command.senderUserId(), command.content());

    ChatMessage savedChatMessage = chatMessagePort.save(chatMessage);

    ChatEvent savedChatEvent = chatEventPort.save(
        ChatEvent.message(
            savedChatMessage.getInquiryId(),
            savedChatMessage.getSenderUserId(),
            savedChatMessage.getId()));

    return new SendChatMessageResult(savedChatMessage, savedChatEvent);
  }
}
