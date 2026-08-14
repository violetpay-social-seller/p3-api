package io.point3.p3api.chat.application.timeline.result;

import io.point3.p3api.chat.domain.entity.ChatEvent;
import io.point3.p3api.chat.domain.entity.ChatMessage;
import io.point3.p3api.chat.domain.type.ChatEventType;
import java.time.Instant;
import java.util.UUID;

public record ChatTimelineItem(
    UUID eventId, ChatEventType type, UUID senderUserId, Instant createdAt, String content) {

  public static ChatTimelineItem from(ChatEvent chatEvent, ChatMessage chatMessage) {
    return new ChatTimelineItem(
        chatEvent.getId(),
        chatEvent.getType(),
        chatEvent.getSenderUserId(),
        chatEvent.getCreatedAt(),
        chatMessage == null ? null : chatMessage.getContent());
  }
}
