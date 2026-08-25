package io.point3.p3api.chat.infrastructure.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.chat.controller.response.ChatTimelineItemStompResponse;
import io.point3.p3api.chat.domain.type.ChatTimelineItemType;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChatMessageRedisEventSerializerTest {

  private final ChatMessageRedisEventSerializer serializer =
      new ChatMessageRedisEventSerializer(new ObjectMapper().findAndRegisterModules());

  @Test
  @DisplayName("Instant를 포함한 채팅 Redis 이벤트를 직렬화하고 원본대로 역직렬화한다")
  void serializesAndDeserializesEventIncludingInstant() {
    ChatMessageRedisEvent event = new ChatMessageRedisEvent(
        UUID.randomUUID(),
        new ChatTimelineItemStompResponse(
            UUID.randomUUID(),
            ChatTimelineItemType.MESSAGE,
            UUID.randomUUID(),
            Instant.parse("2026-08-17T10:00:00Z"),
            "안녕하세요",
            List.of()));

    String serialized = serializer.serialize(event);
    ChatMessageRedisEvent deserialized =
        serializer.deserialize(serialized.getBytes(StandardCharsets.UTF_8));

    assertEquals(event, deserialized);
  }

  @Test
  @DisplayName("형식이 잘못된 Redis payload는 IllegalArgumentException으로 변환한다")
  void throwsIllegalArgumentExceptionForMalformedPayload() {
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> serializer.deserialize("not-json".getBytes(StandardCharsets.UTF_8)));

    assertInstanceOf(JsonProcessingException.class, exception.getCause());
  }
}
