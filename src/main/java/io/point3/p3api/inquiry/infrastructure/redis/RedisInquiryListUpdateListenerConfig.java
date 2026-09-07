package io.point3.p3api.inquiry.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/** 모든 App 인스턴스의 문의 목록 갱신 Redis 채널 구독을 설정한다. */
@Configuration
@Profile("!local-scenario")
@RequiredArgsConstructor
public class RedisInquiryListUpdateListenerConfig {

  private final RedisInquiryListUpdateSubscriber redisInquiryListUpdateSubscriber;

  @Bean
  public RedisMessageListenerContainer redisInquiryListUpdateListenerContainer(
      RedisConnectionFactory redisConnectionFactory) {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(redisConnectionFactory);
    container.addMessageListener(
        redisInquiryListUpdateSubscriber, new ChannelTopic(InquiryListRedisChannel.UPDATES));
    return container;
  }
}
