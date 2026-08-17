package io.point3.p3api.chat.infrastructure.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.chat.application.send.SendChatMessageResult;
import io.point3.p3api.chat.controller.ChatStompDestination;
import io.point3.p3api.chat.controller.response.ChatTimelineItemStompResponse;
import io.point3.p3api.chat.domain.entity.ChatMessage;
import io.point3.p3api.chat.domain.entity.ChatTimelineItem;
import io.point3.p3api.chat.domain.type.ChatTimelineItemType;
import java.time.Instant;
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
class RedisChatMessagePubSubIntegrationTest {

  private static final int REDIS_PORT = 6379;

  @Container
  private static final GenericContainer<?> REDIS = new GenericContainer<>(
      DockerImageName.parse("redis:7.4-alpine")).withExposedPorts(REDIS_PORT);

  private LettuceConnectionFactory connectionFactory;
  private RedisMessageListenerContainer listenerContainer;

  @BeforeEach
  void setUp() {
    connectionFactory = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(REDIS_PORT));
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
    ChatTimelineItemStompResponse response = new ChatTimelineItemStompResponse(
        UUID.randomUUID(),
        ChatTimelineItemType.MESSAGE,
        UUID.randomUUID(),
        Instant.now(),
        "안녕하세요");
    CountDownLatch forwarded = new CountDownLatch(1);
    AtomicReference<String> destination = new AtomicReference<>();
    AtomicReference<ChatTimelineItemStompResponse> forwardedResponse = new AtomicReference<>();
    SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
    doAnswer(invocation -> {
      destination.set(invocation.getArgument(0));
      forwardedResponse.set(invocation.getArgument(1));
      forwarded.countDown();
      return null;
    }).when(messagingTemplate).convertAndSend(
        anyString(), any(ChatTimelineItemStompResponse.class));

    ChatMessageRedisEventSerializer eventSerializer = new ChatMessageRedisEventSerializer(
        new ObjectMapper().findAndRegisterModules());
    RedisChatMessageSubscriber subscriber = new RedisChatMessageSubscriber(
        eventSerializer, messagingTemplate);
    listenerContainer = createListenerContainer(subscriber);
    listenerContainer.start();
    awaitListening();

    RedisChatMessagePublisher publisher = new RedisChatMessagePublisher(
        stringRedisTemplate(), eventSerializer);
    publisher.publish(sendResult(inquiryId, response));

    assertTrue(forwarded.await(5, TimeUnit.SECONDS), "STOMP topic으로 메시지가 전달되지 않았습니다.");
    assertEquals(ChatStompDestination.topicDestination(inquiryId), destination.get());
    assertEquals(response, forwardedResponse.get());
  }

  private void awaitListening() throws InterruptedException {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
    while (!listenerContainer.isListening() && System.nanoTime() < deadline) {
      Thread.sleep(10);
    }
    assertTrue(listenerContainer.isListening(), "Redis 구독 리스너가 준비되지 않았습니다.");
  }

  private RedisMessageListenerContainer createListenerContainer(
      RedisChatMessageSubscriber subscriber) {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.addMessageListener(subscriber, new ChannelTopic(ChatRedisChannel.MESSAGES));
    container.afterPropertiesSet();
    return container;
  }

  private StringRedisTemplate stringRedisTemplate() {
    StringRedisTemplate stringRedisTemplate = new StringRedisTemplate(connectionFactory);
    stringRedisTemplate.afterPropertiesSet();
    return stringRedisTemplate;
  }

  private SendChatMessageResult sendResult(
      UUID inquiryId, ChatTimelineItemStompResponse response) {
    ChatMessage chatMessage = mock(ChatMessage.class);
    when(chatMessage.getInquiryId()).thenReturn(inquiryId);
    when(chatMessage.getContent()).thenReturn(response.content());

    ChatTimelineItem chatTimelineItem = mock(ChatTimelineItem.class);
    when(chatTimelineItem.getId()).thenReturn(response.eventId());
    when(chatTimelineItem.getType()).thenReturn(response.type());
    when(chatTimelineItem.getSenderUserId()).thenReturn(response.senderUserId());
    when(chatTimelineItem.getCreatedAt()).thenReturn(response.createdAt());

    return new SendChatMessageResult(chatMessage, chatTimelineItem);
  }
}
