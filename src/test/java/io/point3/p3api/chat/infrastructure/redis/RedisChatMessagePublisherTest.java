package io.point3.p3api.chat.infrastructure.redis;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.point3.p3api.chat.application.send.SendChatMessageResult;
import io.point3.p3api.chat.domain.entity.ChatMessage;
import io.point3.p3api.chat.domain.entity.ChatTimelineItem;
import io.point3.p3api.chat.domain.type.ChatTimelineItemType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

class RedisChatMessagePublisherTest {

  @Test
  @DisplayName("Redis 발행이 실패해도 예외를 전파하지 않는다")
  void doesNotPropagateExceptionWhenRedisPublishingFails() {
    StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
    ChatMessageRedisEventSerializer eventSerializer = mock(ChatMessageRedisEventSerializer.class);
    when(eventSerializer.serialize(any(ChatMessageRedisEvent.class))).thenReturn("payload");
    doThrow(new IllegalStateException("Redis is unavailable"))
        .when(stringRedisTemplate)
        .convertAndSend(ChatRedisChannel.MESSAGES, "payload");
    RedisChatMessagePublisher publisher =
        new RedisChatMessagePublisher(stringRedisTemplate, eventSerializer);

    assertDoesNotThrow(() -> publisher.publish(sendResult()));

    verify(stringRedisTemplate).convertAndSend(eq(ChatRedisChannel.MESSAGES), eq("payload"));
  }

  private SendChatMessageResult sendResult() {
    ChatMessage chatMessage = mock(ChatMessage.class);
    when(chatMessage.getInquiryId()).thenReturn(UUID.randomUUID());
    when(chatMessage.getContent()).thenReturn("안녕하세요");

    ChatTimelineItem chatTimelineItem = mock(ChatTimelineItem.class);
    when(chatTimelineItem.getId()).thenReturn(UUID.randomUUID());
    when(chatTimelineItem.getType()).thenReturn(ChatTimelineItemType.MESSAGE);
    when(chatTimelineItem.getSenderUserId()).thenReturn(UUID.randomUUID());
    when(chatTimelineItem.getCreatedAt()).thenReturn(Instant.parse("2026-08-17T10:00:00Z"));

    return new SendChatMessageResult(chatMessage, chatTimelineItem, List.of());
  }
}
