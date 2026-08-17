package io.point3.p3api.chat.infrastructure.redis;

import io.point3.p3api.chat.controller.response.ChatTimelineItemStompResponse;
import java.util.UUID;

/** Redis 채널로 전파하는 문의방 메시지 이벤트다. */
public record ChatMessageRedisEvent(UUID inquiryId, ChatTimelineItemStompResponse message) {}
