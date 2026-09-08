package io.point3.p3api.chat.infrastructure.redis;

import io.point3.p3api.chat.application.port.ChatTimelineRealtimePublisherPort;
import io.point3.p3api.chat.application.realtime.ChatTimelineRealtimeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** 저장된 채팅 타임라인 항목을 Redis Pub/Sub 채널로 발행한다. */
@Slf4j
@Component
@Profile("!local-scenario")
@RequiredArgsConstructor
public class RedisChatTimelinePublisher implements ChatTimelineRealtimePublisherPort {

  private final StringRedisTemplate stringRedisTemplate;
  private final ChatTimelineRedisEventSerializer eventSerializer;

  @Override
  public void publish(ChatTimelineRealtimeEvent event) {
    ChatTimelineRedisEvent redisEvent =
        new ChatTimelineRedisEvent(event.inquiryId(), event.payload());

    try {
      stringRedisTemplate.convertAndSend(
          ChatRedisChannel.TIMELINE_ITEMS, eventSerializer.serialize(redisEvent));
    } catch (RuntimeException e) {
      log.error(
          "Failed to publish Redis chat timeline event: inquiryId={}, eventId={}",
          event.inquiryId(),
          event.payload().eventId(),
          e);
    }
  }
}
