package io.point3.p3api.chat.infrastructure.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.chat.application.realtime.ChatTimelineRealtimePayload;
import io.point3.p3api.chat.domain.type.ChatTimelineItemType;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChatTimelineRedisEventSerializerTest {

  private final ChatTimelineRedisEventSerializer serializer =
      new ChatTimelineRedisEventSerializer(new ObjectMapper().findAndRegisterModules());

  @Test
  @DisplayName("Instant와 referenceId를 포함한 채팅 타임라인 Redis 이벤트를 원본대로 복원한다")
  void serializesAndDeserializesEventIncludingInstant() {
    ChatTimelineRedisEvent event = new ChatTimelineRedisEvent(
        UUID.randomUUID(),
        new ChatTimelineRealtimePayload(
            UUID.randomUUID(),
            UUID.randomUUID(),
            ChatTimelineItemType.ORDER_FORM_SUBMISSION,
            UUID.randomUUID(),
            Instant.parse("2026-08-17T10:00:00Z"),
            null,
            List.of()));

    String serialized = serializer.serialize(event);
    ChatTimelineRedisEvent deserialized =
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
