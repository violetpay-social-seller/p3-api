package io.point3.p3api.chat.infrastructure.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.chat.application.realtime.ChatTimelineRealtimeEvent;
import io.point3.p3api.chat.application.realtime.ChatTimelineRealtimePayload;
import io.point3.p3api.chat.controller.ChatStompDestination;
import io.point3.p3api.chat.domain.type.ChatTimelineItemType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class RedisChatTimelinePubSubIntegrationTest {

  private static final int REDIS_PORT = 6379;

  @Container
  private static final GenericContainer<?> REDIS = new GenericContainer<>(
          DockerImageName.parse("redis:7.4-alpine"))
      .withExposedPorts(REDIS_PORT);

  private LettuceConnectionFactory connectionFactory;
  private RedisMessageListenerContainer listenerContainer;

  @BeforeEach
  void setUp() {
    connectionFactory =
        new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(REDIS_PORT));
    connectionFactory.afterPropertiesSet();
    connectionFactory.start();
  }

  @AfterEach
  void tearDown() throws Exception {
    if (listenerContainer != null) {
      listenerContainer.stop();
      listenerContainer.destroy();
    }
    connectionFactory.destroy();
  }

  @Test
  @DisplayName("Redis 발행 이벤트를 수신해 로컬 STOMP 문의방 토픽으로 전달한다")
  void publishesRedisEventAndForwardsItToLocalStompTopic() throws InterruptedException {
    UUID inquiryId = UUID.randomUUID();
    ChatTimelineRealtimePayload payload = new ChatTimelineRealtimePayload(
        UUID.randomUUID(),
        UUID.randomUUID(),
        ChatTimelineItemType.ORDER_CONFIRMATION,
        UUID.randomUUID(),
        Instant.now(),
        null,
        List.of());
    CountDownLatch forwarded = new CountDownLatch(1);
    AtomicReference<String> destination = new AtomicReference<>();
    AtomicReference<ChatTimelineRealtimePayload> forwardedPayload = new AtomicReference<>();
    SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
    doAnswer(invocation -> {
          destination.set(invocation.getArgument(0));
          forwardedPayload.set(invocation.getArgument(1));
          forwarded.countDown();
          return null;
        })
        .when(messagingTemplate)
        .convertAndSend(anyString(), any(ChatTimelineRealtimePayload.class));

    ChatTimelineRedisEventSerializer eventSerializer =
        new ChatTimelineRedisEventSerializer(new ObjectMapper().findAndRegisterModules());
    RedisChatTimelineSubscriber subscriber =
        new RedisChatTimelineSubscriber(eventSerializer, messagingTemplate);
    listenerContainer = createListenerContainer(subscriber);
    listenerContainer.start();
    awaitListening();

    RedisChatTimelinePublisher publisher =
        new RedisChatTimelinePublisher(stringRedisTemplate(), eventSerializer);
    publisher.publish(new ChatTimelineRealtimeEvent(inquiryId, payload));

    assertTrue(forwarded.await(5, TimeUnit.SECONDS), "STOMP topic으로 타임라인 이벤트가 전달되지 않았습니다.");
    assertEquals(ChatStompDestination.topicDestination(inquiryId), destination.get());
    assertEquals(payload, forwardedPayload.get());
  }

  private void awaitListening() throws InterruptedException {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
    while (!listenerContainer.isListening() && System.nanoTime() < deadline) {
      Thread.sleep(10);
    }
    assertTrue(listenerContainer.isListening(), "Redis 구독 리스너가 준비되지 않았습니다.");
  }

  private RedisMessageListenerContainer createListenerContainer(
      RedisChatTimelineSubscriber subscriber) {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.addMessageListener(subscriber, new ChannelTopic(ChatRedisChannel.TIMELINE_ITEMS));
    container.afterPropertiesSet();
    return container;
  }

  private StringRedisTemplate stringRedisTemplate() {
    StringRedisTemplate stringRedisTemplate = new StringRedisTemplate(connectionFactory);
    stringRedisTemplate.afterPropertiesSet();
    return stringRedisTemplate;
  }
}
