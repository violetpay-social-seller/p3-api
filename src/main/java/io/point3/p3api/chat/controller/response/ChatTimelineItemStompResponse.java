package io.point3.p3api.chat.controller.response;

import io.point3.p3api.chat.application.send.SendChatMessageResult;
import io.point3.p3api.chat.domain.type.ChatTimelineItemType;
import java.time.Instant;
import java.util.UUID;

/** 문의방 구독자에게 STOMP로 전달하는 채팅 타임라인 응답이다. */
public record ChatTimelineItemStompResponse(
    UUID eventId, ChatTimelineItemType type, UUID senderUserId, Instant createdAt, String content) {

  public static ChatTimelineItemStompResponse from(SendChatMessageResult result) {
    return new ChatTimelineItemStompResponse(
        result.chatTimelineItem().getId(),
        result.chatTimelineItem().getType(),
        result.chatTimelineItem().getSenderUserId(),
        result.chatTimelineItem().getCreatedAt(),
        result.chatMessage().getContent());
  }
}
