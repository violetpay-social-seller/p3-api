package io.point3.p3api.chat.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/** 모든 App 인스턴스에서 Redis 채팅 이벤트를 수신한다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatMessageSubscriber implements MessageListener {

  private final ChatMessageRedisEventSerializer eventSerializer;

  @Override
  public void onMessage(Message message, byte[] pattern) {
    ChatMessageRedisEvent event = eventSerializer.deserialize(message.getBody());
    log.debug(
        "Received Redis chat event: inquiryId={}, eventId={}",
        event.inquiryId(),
        event.message().eventId());
  }
}
