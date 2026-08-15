package io.point3.p3api.chat.application.timeline.result;

import io.point3.p3api.chat.domain.entity.ChatMessage;
import io.point3.p3api.chat.domain.entity.ChatTimelineItem;
import io.point3.p3api.chat.domain.type.ChatTimelineItemType;
import java.time.Instant;
import java.util.UUID;

public record ChatTimelineItemResult(
    UUID eventId, ChatTimelineItemType type, UUID senderUserId, Instant createdAt, String content) {

  public static ChatTimelineItemResult from(
      ChatTimelineItem chatTimelineItem, ChatMessage chatMessage) {
    return new ChatTimelineItemResult(
        chatTimelineItem.getId(),
        chatTimelineItem.getType(),
        chatTimelineItem.getSenderUserId(),
        chatTimelineItem.getCreatedAt(),
        chatMessage == null ? null : chatMessage.getContent());
  }
}
