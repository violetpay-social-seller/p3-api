package io.point3.p3api.chat.infrastructure.redis;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.point3.p3api.chat.application.realtime.ChatTimelineRealtimeEvent;
import io.point3.p3api.chat.application.realtime.ChatTimelineRealtimePayload;
import io.point3.p3api.chat.domain.type.ChatTimelineItemType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

class RedisChatTimelinePublisherTest {

  @Test
  @DisplayName("Redis 발행이 실패해도 예외를 전파하지 않는다")
  void doesNotPropagateExceptionWhenRedisPublishingFails() {
    StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
    ChatTimelineRedisEventSerializer eventSerializer =
        mock(ChatTimelineRedisEventSerializer.class);
    when(eventSerializer.serialize(any(ChatTimelineRedisEvent.class))).thenReturn("payload");
    doThrow(new IllegalStateException("Redis is unavailable"))
        .when(stringRedisTemplate)
        .convertAndSend(ChatRedisChannel.TIMELINE_ITEMS, "payload");
    RedisChatTimelinePublisher publisher =
        new RedisChatTimelinePublisher(stringRedisTemplate, eventSerializer);

    assertDoesNotThrow(() -> publisher.publish(event()));

    verify(stringRedisTemplate).convertAndSend(eq(ChatRedisChannel.TIMELINE_ITEMS), eq("payload"));
  }

  private ChatTimelineRealtimeEvent event() {
    return new ChatTimelineRealtimeEvent(
        UUID.randomUUID(),
        new ChatTimelineRealtimePayload(
            UUID.randomUUID(),
            UUID.randomUUID(),
            ChatTimelineItemType.PAYMENT_COMPLETED,
            UUID.randomUUID(),
            Instant.parse("2026-08-17T10:00:00Z"),
            null,
            List.of()));
  }
}
