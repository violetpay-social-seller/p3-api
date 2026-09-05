package io.point3.p3api.chat.application.timeline.query;

import io.point3.p3api.chat.application.port.ChatMessageAssetPort;
import io.point3.p3api.chat.application.port.ChatMessagePort;
import io.point3.p3api.chat.application.port.ChatTimelineItemPort;
import io.point3.p3api.chat.application.timeline.result.ChatTimelineItemResult;
import io.point3.p3api.chat.application.timeline.result.ChatTimelinePage;
import io.point3.p3api.chat.domain.entity.ChatMessage;
import io.point3.p3api.chat.domain.entity.ChatMessageAsset;
import io.point3.p3api.chat.domain.entity.ChatTimelineItem;
import io.point3.p3api.chat.domain.type.ChatTimelineItemType;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

  private final ChatTimelineItemPort chatTimelineItemPort;
  private final ChatMessagePort chatMessagePort;
  private final ChatMessageAssetPort chatMessageAssetPort;

  @Override
  public ChatTimelinePage execute(UUID inquiryId, ChatTimelineQuery query) {
    validateCursor(query);

    int size = resolvePageSize(query.size());
    List<ChatTimelineItem> chatTimelineItems = chatTimelineItemPort.findTimeline(
        inquiryId, query.cursorCreatedAt(), query.cursorId(), size + 1);
    boolean hasNext = chatTimelineItems.size() > size;
    List<ChatTimelineItem> pageItems =
        hasNext ? chatTimelineItems.subList(0, size) : chatTimelineItems;

    Map<UUID, ChatMessage> chatMessages = findMessages(pageItems);
    Map<UUID, List<ChatMessageAsset>> chatMessageAssets = findMessageAssets(chatMessages.keySet());
    List<ChatTimelineItemResult> items = pageItems.stream()
        .map(chatTimelineItem -> ChatTimelineItemResult.from(
            chatTimelineItem,
            chatMessages.get(chatTimelineItem.getReferenceId()),
            chatMessageAssets.getOrDefault(chatTimelineItem.getReferenceId(), List.of())))
        .toList();
    List<ChatTimelineItemResult> chronologicalItems = new ArrayList<>(items);
    Collections.reverse(chronologicalItems);

    ChatTimelineItem lastItem = hasNext ? pageItems.getLast() : null;
    return new ChatTimelinePage(
        chronologicalItems,
        hasNext,
        lastItem == null ? null : lastItem.getCreatedAt(),
        lastItem == null ? null : lastItem.getId());
  }

  private Map<UUID, ChatMessage> findMessages(List<ChatTimelineItem> chatTimelineItems) {
    List<UUID> messageIds = chatTimelineItems.stream()
        .filter(chatTimelineItem -> chatTimelineItem.getType() == ChatTimelineItemType.MESSAGE)
        .map(ChatTimelineItem::getReferenceId)
        .toList();

    Map<UUID, ChatMessage> messages = new HashMap<>();
    chatMessagePort
        .findAllById(messageIds)
        .forEach(message -> messages.put(message.getId(), message));
    return messages;
  }

  private Map<UUID, List<ChatMessageAsset>> findMessageAssets(Collection<UUID> messageIds) {
    Map<UUID, List<ChatMessageAsset>> messageAssets = new HashMap<>();

    if (messageIds.isEmpty()) {
      return messageAssets;
    }

    chatMessageAssetPort.findAllByMessageIdIn(messageIds).forEach(asset -> messageAssets
        .computeIfAbsent(asset.getMessageId(), ignored -> new ArrayList<>())
        .add(asset));
    return messageAssets;
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
