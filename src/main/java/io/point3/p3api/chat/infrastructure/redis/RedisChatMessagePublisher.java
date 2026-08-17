package io.point3.p3api.chat.infrastructure.redis;

import io.point3.p3api.chat.application.port.ChatMessageRealtimePublisherPort;
import io.point3.p3api.chat.application.send.SendChatMessageResult;
import io.point3.p3api.chat.controller.response.ChatTimelineItemStompResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** 저장된 채팅 메시지를 Redis Pub/Sub 채널로 발행한다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatMessagePublisher implements ChatMessageRealtimePublisherPort {

  private final StringRedisTemplate stringRedisTemplate;
  private final ChatMessageRedisEventSerializer eventSerializer;

  @Override
  public void publish(SendChatMessageResult result) {
    ChatMessageRedisEvent event = new ChatMessageRedisEvent(
        result.chatMessage().getInquiryId(), ChatTimelineItemStompResponse.from(result));

    try {
      stringRedisTemplate.convertAndSend(
          ChatRedisChannel.MESSAGES, eventSerializer.serialize(event));
    } catch (RuntimeException e) {
      log.error(
          "Failed to publish Redis chat event: inquiryId={}, eventId={}",
          event.inquiryId(),
          event.message().eventId(),
          e);
    }
  }
}
