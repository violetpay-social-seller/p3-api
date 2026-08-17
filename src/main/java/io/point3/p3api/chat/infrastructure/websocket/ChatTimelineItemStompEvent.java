package io.point3.p3api.chat.infrastructure.websocket;

import io.point3.p3api.chat.application.send.SendChatMessageResult;
import io.point3.p3api.chat.domain.type.ChatTimelineItemType;
import java.time.Instant;
import java.util.UUID;

/** 문의방 구독자에게 STOMP로 전달하는 채팅 타임라인 이벤트 */
public record ChatTimelineItemStompEvent(
    UUID eventId,
    ChatTimelineItemType type,
    UUID senderUserId,
    Instant createdAt,
    String content) {

  public static ChatTimelineItemStompEvent from(SendChatMessageResult result) {
    return new ChatTimelineItemStompEvent(
        result.chatTimelineItem().getId(),
        result.chatTimelineItem().getType(),
        result.chatTimelineItem().getSenderUserId(),
        result.chatTimelineItem().getCreatedAt(),
        result.chatMessage().getContent());
  }
}
