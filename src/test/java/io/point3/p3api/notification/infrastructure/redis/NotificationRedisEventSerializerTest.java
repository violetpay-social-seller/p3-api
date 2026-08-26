package io.point3.p3api.notification.infrastructure.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.notification.application.result.NotificationResult;
import io.point3.p3api.notification.domain.type.NotificationReferenceType;
import io.point3.p3api.notification.domain.type.NotificationType;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotificationRedisEventSerializerTest {

  private final NotificationRedisEventSerializer serializer =
      new NotificationRedisEventSerializer(new ObjectMapper().findAndRegisterModules());

  @Test
  @DisplayName("수신 사용자와 알림을 Redis 이벤트로 직렬화하고 원본대로 역직렬화한다")
  void serializesAndDeserializesEvent() {
    NotificationRedisEvent event = new NotificationRedisEvent(
        UUID.randomUUID(),
        new NotificationResult(
            UUID.randomUUID(),
            NotificationType.PAYMENT_COMPLETED,
            NotificationReferenceType.ORDER,
            UUID.randomUUID(),
            "결제가 완료되었습니다.",
            "주문 내역을 확인해 주세요.",
            null,
            Instant.parse("2026-08-26T06:00:00Z")));

    String serialized = serializer.serialize(event);
    NotificationRedisEvent deserialized =
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
