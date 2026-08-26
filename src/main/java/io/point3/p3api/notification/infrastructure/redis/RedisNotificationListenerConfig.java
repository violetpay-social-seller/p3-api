package io.point3.p3api.notification.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/** 모든 App 인스턴스의 알림 Redis 채널 구독을 설정한다. */
@Configuration
@Profile("!local-scenario")
@RequiredArgsConstructor
public class RedisNotificationListenerConfig {
  private final RedisNotificationSubscriber redisNotificationSubscriber;

  @Bean
  public RedisMessageListenerContainer redisNotificationListenerContainer(
      RedisConnectionFactory redisConnectionFactory) {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(redisConnectionFactory);
    container.addMessageListener(
        redisNotificationSubscriber, new ChannelTopic(NotificationRedisChannel.CREATED));
    return container;
  }
}
