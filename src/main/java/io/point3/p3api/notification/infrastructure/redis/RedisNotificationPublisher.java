package io.point3.p3api.notification.infrastructure.redis;

import io.point3.p3api.notification.application.realtime.NotificationCreatedEvent;
import io.point3.p3api.notification.application.realtime.NotificationRealtimePublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** 다중 인스턴스에 저장 완료 알림을 전파한다. */
@Slf4j
@Component
@Profile("!local-scenario")
@RequiredArgsConstructor
public class RedisNotificationPublisher implements NotificationRealtimePublisherPort {
  private final StringRedisTemplate stringRedisTemplate;
  private final NotificationRedisEventSerializer notificationRedisEventSerializer;

  @Override
  public void publish(NotificationCreatedEvent event) {
    try {
      stringRedisTemplate.convertAndSend(
          NotificationRedisChannel.CREATED,
          notificationRedisEventSerializer.serialize(
              new NotificationRedisEvent(event.userId(), event.notification())));
    } catch (RuntimeException e) {
      log.error(
          "Failed to publish notification event: notificationId={}",
          event.notification().id(),
          e);
    }
  }
}
