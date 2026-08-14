package io.point3.p3api.inquiry.controller.response;

import io.point3.p3api.chat.application.send.SendChatMessageResult;
import io.point3.p3api.chat.application.timeline.result.ChatTimelineItem;
import io.point3.p3api.chat.domain.type.ChatEventType;
import java.time.Instant;
import java.util.UUID;

public record ChatTimelineItemResponse(
    UUID eventId, ChatEventType type, UUID senderUserId, Instant createdAt, String content) {

  public static ChatTimelineItemResponse from(SendChatMessageResult result) {
    return new ChatTimelineItemResponse(
        result.chatEvent().getId(),
        result.chatEvent().getType(),
        result.chatEvent().getSenderUserId(),
        result.chatEvent().getCreatedAt(),
        result.chatMessage().getContent());
  }

  public static ChatTimelineItemResponse from(ChatTimelineItem item) {
    return new ChatTimelineItemResponse(
        item.eventId(), item.type(), item.senderUserId(), item.createdAt(), item.content());
  }
}
