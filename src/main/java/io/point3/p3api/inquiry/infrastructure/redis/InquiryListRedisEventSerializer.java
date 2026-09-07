package io.point3.p3api.inquiry.infrastructure.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InquiryListRedisEventSerializer {

  private final ObjectMapper objectMapper;

  public String serialize(InquiryListRedisEvent event) {
    try {
      return objectMapper.writeValueAsString(event);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to serialize inquiry list Redis event", e);
    }
  }

  public InquiryListRedisEvent deserialize(byte[] payload) {
    try {
      return objectMapper.readValue(
          new String(payload, StandardCharsets.UTF_8), InquiryListRedisEvent.class);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Failed to deserialize inquiry list Redis event", e);
    }
  }
}
