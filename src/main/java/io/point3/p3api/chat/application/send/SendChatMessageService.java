package io.point3.p3api.chat.application.send;

import io.point3.p3api.chat.application.port.ChatMessagePort;
import io.point3.p3api.chat.application.timeline.ChatTimelineItemPublisher;
import io.point3.p3api.chat.domain.entity.ChatMessage;
import io.point3.p3api.chat.domain.entity.ChatTimelineItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SendChatMessageService implements SendChatMessageUseCase {

  private final ChatMessagePort chatMessagePort;
  private final ChatTimelineItemPublisher chatTimelineItemPublisher;

  @Override
  @Transactional
  public SendChatMessageResult execute(SendChatMessageCommand command) {
    ChatMessage chatMessage =
        ChatMessage.create(command.inquiryId(), command.senderUserId(), command.content());

    ChatMessage savedChatMessage = chatMessagePort.save(chatMessage);

    ChatTimelineItem savedChatTimelineItem = chatTimelineItemPublisher.publishMessage(
        savedChatMessage.getInquiryId(),
        savedChatMessage.getSenderUserId(),
        savedChatMessage.getId());

    return new SendChatMessageResult(savedChatMessage, savedChatTimelineItem);
  }
}
