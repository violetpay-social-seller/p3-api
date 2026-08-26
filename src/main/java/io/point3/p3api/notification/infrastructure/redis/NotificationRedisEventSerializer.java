package io.point3.p3api.notification.infrastructure.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationRedisEventSerializer {
  private final ObjectMapper objectMapper;

  public String serialize(NotificationRedisEvent event) {
    try {
      return objectMapper.writeValueAsString(event);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to serialize notification event", e);
    }
  }

  public NotificationRedisEvent deserialize(byte[] payload) {
    try {
      return objectMapper.readValue(
          new String(payload, StandardCharsets.UTF_8), NotificationRedisEvent.class);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Failed to deserialize notification event", e);
    }
  }
}
