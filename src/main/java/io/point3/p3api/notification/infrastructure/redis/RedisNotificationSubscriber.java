package io.point3.p3api.notification.infrastructure.redis;

import io.point3.p3api.notification.infrastructure.sse.NotificationSseConnectionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/** 모든 App 인스턴스에서 Redis 알림 이벤트를 수신한다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisNotificationSubscriber implements MessageListener {
  private final NotificationRedisEventSerializer notificationRedisEventSerializer;
  private final NotificationSseConnectionRegistry notificationSseConnectionRegistry;

  @Override
  public void onMessage(Message message, byte[] pattern) {
    try {
      NotificationRedisEvent event =
          notificationRedisEventSerializer.deserialize(message.getBody());
      notificationSseConnectionRegistry.send(event.userId(), event.notification());
    } catch (RuntimeException e) {
      log.error("Failed to process Redis notification event", e);
    }
  }
}
