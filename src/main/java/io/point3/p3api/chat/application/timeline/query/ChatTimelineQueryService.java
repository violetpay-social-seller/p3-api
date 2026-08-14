package io.point3.p3api.chat.application.timeline.query;

import io.point3.p3api.chat.application.port.ChatEventPort;
import io.point3.p3api.chat.application.port.ChatMessagePort;
import io.point3.p3api.chat.application.timeline.result.ChatTimelineItem;
import io.point3.p3api.chat.application.timeline.result.ChatTimelinePage;
import io.point3.p3api.chat.domain.entity.ChatEvent;
import io.point3.p3api.chat.domain.entity.ChatMessage;
import io.point3.p3api.chat.domain.type.ChatEventType;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatTimelineQueryService implements ChatTimelineQueryUseCase {

  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int MAX_PAGE_SIZE = 100;

  private final ChatEventPort chatEventPort;
  private final ChatMessagePort chatMessagePort;

  @Override
  public ChatTimelinePage execute(UUID inquiryId, ChatTimelineQuery query) {
    validateCursor(query);

    int size = resolvePageSize(query.size());
    List<ChatEvent> chatEvents = chatEventPort.findTimeline(
        inquiryId, query.cursorCreatedAt(), query.cursorId(), size + 1);
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

  private void validateCursor(ChatTimelineQuery query) {
    if ((query.cursorCreatedAt() == null) != (query.cursorId() == null)) {
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
}
