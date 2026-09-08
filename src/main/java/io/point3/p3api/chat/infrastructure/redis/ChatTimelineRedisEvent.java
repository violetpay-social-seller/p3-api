package io.point3.p3api.chat.infrastructure.redis;

import io.point3.p3api.chat.application.realtime.ChatTimelineRealtimePayload;
import java.util.UUID;

/** Redis 채널로 전파하는 문의방 타임라인 이벤트 */
public record ChatTimelineRedisEvent(UUID inquiryId, ChatTimelineRealtimePayload payload) {}
