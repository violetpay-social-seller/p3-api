package io.point3.p3api.chat.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/** 모든 App 인스턴스의 채팅 Redis 채널 구독을 설정한다. */
@Configuration
@RequiredArgsConstructor
public class RedisChatMessageListenerConfig {

  private final RedisChatMessageSubscriber redisChatMessageSubscriber;

  @Bean
  public RedisMessageListenerContainer redisChatMessageListenerContainer(
      RedisConnectionFactory redisConnectionFactory) {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(redisConnectionFactory);
    container.addMessageListener(
        redisChatMessageSubscriber, new ChannelTopic(ChatRedisChannel.MESSAGES));
    return container;
  }
}
