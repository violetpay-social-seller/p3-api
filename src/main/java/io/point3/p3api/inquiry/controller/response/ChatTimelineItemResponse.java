package io.point3.p3api.inquiry.controller.response;

import io.point3.p3api.chat.application.send.SendChatMessageResult;
import io.point3.p3api.chat.application.timeline.result.ChatTimelineItemResult;
import io.point3.p3api.chat.domain.type.ChatTimelineItemType;
import java.time.Instant;
import java.util.UUID;

public record ChatTimelineItemResponse(
    UUID eventId,
    ChatTimelineItemType type,
    UUID senderUserId,
    Instant createdAt,
    String content
) {

  public static ChatTimelineItemResponse from(SendChatMessageResult result) {
    return new ChatTimelineItemResponse(
        result.chatTimelineItem().getId(),
        result.chatTimelineItem().getType(),
        result.chatTimelineItem().getSenderUserId(),
        result.chatTimelineItem().getCreatedAt(),
        result.chatMessage().getContent());
  }

  public static ChatTimelineItemResponse from(ChatTimelineItemResult item) {
    return new ChatTimelineItemResponse(
        item.eventId(), item.type(), item.senderUserId(), item.createdAt(), item.content());
  }
}
