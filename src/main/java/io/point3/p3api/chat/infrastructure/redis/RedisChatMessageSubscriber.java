package io.point3.p3api.chat.infrastructure.redis;

import io.point3.p3api.chat.controller.ChatStompDestination;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** 모든 App 인스턴스에서 Redis 채팅 이벤트를 수신한다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatMessageSubscriber implements MessageListener {

  private final ChatMessageRedisEventSerializer eventSerializer;
  private final SimpMessagingTemplate messagingTemplate;

  @Override
  public void onMessage(Message message, byte[] pattern) {
    try {
      ChatMessageRedisEvent event = eventSerializer.deserialize(message.getBody());
      messagingTemplate.convertAndSend(
          ChatStompDestination.topicDestination(event.inquiryId()), event.message());
      log.debug(
          "Forwarded Redis chat event to local STOMP topic: inquiryId={}, eventId={}",
          event.inquiryId(),
          event.message().eventId());
    } catch (RuntimeException e) {
      log.error("Failed to process Redis chat event", e);
    }
  }
}
