package io.point3.p3api.chat.infrastructure.websocket;

import io.point3.p3api.chat.domain.type.ChatTimelineItemType;
import java.time.Instant;
import java.util.UUID;

/** 문의방 구독자에게 STOMP로 전달하는 채팅 타임라인 이벤트 */
public record ChatTimelineItemStompEvent(
    UUID eventId,
    ChatTimelineItemType type,
    UUID senderUserId,
    Instant createdAt,
    String content) {}
