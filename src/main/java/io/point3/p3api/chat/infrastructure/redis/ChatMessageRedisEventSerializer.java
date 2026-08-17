package io.point3.p3api.chat.infrastructure.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 채팅 Redis 이벤트를 JSON 문자열로 직렬화·역직렬화한다. */
@Component
@RequiredArgsConstructor
public class ChatMessageRedisEventSerializer {

  private final ObjectMapper objectMapper;

  public String serialize(ChatMessageRedisEvent event) {
    try {
      return objectMapper.writeValueAsString(event);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to serialize chat Redis event", e);
    }
  }

  public ChatMessageRedisEvent deserialize(byte[] payload) {
    try {
      return objectMapper.readValue(
          new String(payload, StandardCharsets.UTF_8), ChatMessageRedisEvent.class);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Failed to deserialize chat Redis event", e);
    }
  }
}
