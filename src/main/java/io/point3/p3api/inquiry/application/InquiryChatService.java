package io.point3.p3api.inquiry.application;

import io.point3.p3api.chat.application.port.ChatEventPort;
import io.point3.p3api.chat.application.port.ChatMessagePort;
import io.point3.p3api.chat.application.send.SendChatMessageCommand;
import io.point3.p3api.chat.application.send.SendChatMessageResult;
import io.point3.p3api.chat.application.send.SendChatMessageUseCase;
import io.point3.p3api.chat.domain.entity.ChatEvent;
import io.point3.p3api.chat.domain.entity.ChatMessage;
import io.point3.p3api.chat.domain.type.ChatEventType;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InquiryChatService {

  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int MAX_PAGE_SIZE = 100;

  private final InquiryChatAccessService inquiryChatAccessService;
  private final SendChatMessageUseCase sendChatMessageUseCase;
  private final ChatEventPort chatEventPort;
  private final ChatMessagePort chatMessagePort;

  public SendChatMessageResult sendBuyerMessage(
      UUID inquiryId, UUID buyerUserId, String content) {
    inquiryChatAccessService.getBuyerInquiry(inquiryId, buyerUserId);
    return sendChatMessageUseCase.execute(new SendChatMessageCommand(inquiryId, buyerUserId, content));
  }

  public SendChatMessageResult sendSellerMessage(UUID inquiryId, UUID storeId, UUID sellerUserId, String content) {
    inquiryChatAccessService.getSellerInquiry(inquiryId, storeId);
    return sendChatMessageUseCase.execute(
        new SendChatMessageCommand(inquiryId, sellerUserId, content));
  }

  @Transactional(readOnly = true)
  public ChatTimelinePage getBuyerTimeline(
      UUID inquiryId, UUID buyerUserId, Instant cursorCreatedAt, UUID cursorId, Integer size) {
    inquiryChatAccessService.getBuyerInquiry(inquiryId, buyerUserId);
    return getTimeline(inquiryId, cursorCreatedAt, cursorId, size);
  }

  @Transactional(readOnly = true)
  public ChatTimelinePage getSellerTimeline(
      UUID inquiryId, UUID storeId, Instant cursorCreatedAt, UUID cursorId, Integer size) {
    inquiryChatAccessService.getSellerInquiry(inquiryId, storeId);
    return getTimeline(inquiryId, cursorCreatedAt, cursorId, size);
  }

  private ChatTimelinePage getTimeline(
      UUID inquiryId, Instant cursorCreatedAt, UUID cursorId, Integer requestedSize) {
    validateCursor(cursorCreatedAt, cursorId);

    int size = resolvePageSize(requestedSize);
    List<ChatEvent> chatEvents =
        chatEventPort.findTimeline(inquiryId, cursorCreatedAt, cursorId, size + 1);
    boolean hasNext = chatEvents.size() > size;
    List<ChatEvent> pageEvents = hasNext ? chatEvents.subList(0, size) : chatEvents;

    Map<UUID, ChatMessage> chatMessages = findMessages(pageEvents);
    List<ChatTimelineItem> items = pageEvents.stream()
        .map(chatEvent -> ChatTimelineItem.from(chatEvent, chatMessages.get(chatEvent.getReferenceId())))
        .toList();

    ChatEvent lastEvent = hasNext ? pageEvents.getLast() : null;
    return new ChatTimelinePage(
        items,
        hasNext,
        lastEvent == null ? null : lastEvent.getCreatedAt(),
        lastEvent == null ? null : lastEvent.getId());
  }

  private Map<UUID, ChatMessage> findMessages(List<ChatEvent> chatEvents) {
    List<UUID> messageIds = chatEvents.stream()
        .filter(chatEvent -> chatEvent.getType() == ChatEventType.MESSAGE)
        .map(ChatEvent::getReferenceId)
        .toList();

    Map<UUID, ChatMessage> messages = new HashMap<>();
    chatMessagePort.findAllById(messageIds).forEach(message -> messages.put(message.getId(), message));
    return messages;
  }

  private void validateCursor(Instant cursorCreatedAt, UUID cursorId) {
    if ((cursorCreatedAt == null) != (cursorId == null)) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT);
    }
  }

  private int resolvePageSize(Integer requestedSize) {
    int size = requestedSize == null ? DEFAULT_PAGE_SIZE : requestedSize;

    if (size < 1 || size > MAX_PAGE_SIZE) {
      throw new BaseException(CommonErrorCode.INVALID_INPUT);
    }

    return size;
  }

  public record ChatTimelinePage(
      List<ChatTimelineItem> items,
      boolean hasNext,
      Instant nextCursorCreatedAt,
      UUID nextCursorId) {}

  public record ChatTimelineItem(
      UUID eventId,
      ChatEventType type,
      UUID senderUserId,
      Instant createdAt,
      String content) {

    private static ChatTimelineItem from(ChatEvent chatEvent, ChatMessage chatMessage) {
      return new ChatTimelineItem(
          chatEvent.getId(),
          chatEvent.getType(),
          chatEvent.getSenderUserId(),
          chatEvent.getCreatedAt(),
          chatMessage == null ? null : chatMessage.getContent());
    }
  }
}
