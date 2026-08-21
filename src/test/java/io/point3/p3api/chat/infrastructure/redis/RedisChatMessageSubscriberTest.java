package io.point3.p3api.chat.infrastructure.redis;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.point3.p3api.chat.controller.ChatStompDestination;
import io.point3.p3api.chat.controller.response.ChatTimelineItemStompResponse;
import io.point3.p3api.chat.domain.type.ChatTimelineItemType;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;

class RedisChatMessageSubscriberTest {

  @Test
  @DisplayName("잘못된 Redis payload를 받아도 리스너 예외를 전파하지 않는다")
  void doesNotPropagateExceptionForMalformedRedisPayload() {
    ChatMessageRedisEventSerializer eventSerializer = mock(ChatMessageRedisEventSerializer.class);
    SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
    Message message = mock(Message.class);
    when(message.getBody()).thenReturn("invalid".getBytes());
    when(eventSerializer.deserialize(message.getBody()))
        .thenThrow(new IllegalArgumentException("Invalid Redis payload"));
    RedisChatMessageSubscriber subscriber =
        new RedisChatMessageSubscriber(eventSerializer, messagingTemplate);

    assertDoesNotThrow(() -> subscriber.onMessage(message, null));

    verifyNoInteractions(messagingTemplate);
  }

  @Test
  @DisplayName("STOMP 전달 실패 뒤에도 다음 Redis 이벤트를 처리한다")
  void continuesProcessingAfterStompForwardingFails() {
    ChatMessageRedisEvent event = event();
    ChatMessageRedisEventSerializer eventSerializer = mock(ChatMessageRedisEventSerializer.class);
    SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
    Message message = mock(Message.class);
    when(message.getBody()).thenReturn("payload".getBytes());
    when(eventSerializer.deserialize(message.getBody())).thenReturn(event);
    String destination = ChatStompDestination.topicDestination(event.inquiryId());
    doThrow(new IllegalStateException("STOMP broker is unavailable"))
        .doNothing()
        .when(messagingTemplate)
        .convertAndSend(destination, event.message());
    RedisChatMessageSubscriber subscriber =
        new RedisChatMessageSubscriber(eventSerializer, messagingTemplate);

    assertDoesNotThrow(() -> subscriber.onMessage(message, null));
    assertDoesNotThrow(() -> subscriber.onMessage(message, null));

    verify(messagingTemplate, times(2)).convertAndSend(destination, event.message());
  }

  private ChatMessageRedisEvent event() {
    return new ChatMessageRedisEvent(
        UUID.randomUUID(),
        new ChatTimelineItemStompResponse(
            UUID.randomUUID(),
            ChatTimelineItemType.MESSAGE,
            UUID.randomUUID(),
            Instant.parse("2026-08-17T10:00:00Z"),
            "안녕하세요"));
  }
}
