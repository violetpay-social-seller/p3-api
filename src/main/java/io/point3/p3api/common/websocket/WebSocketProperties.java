package io.point3.p3api.common.websocket;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "p3.websocket")
public record WebSocketProperties(@NotEmpty List<String> allowedOrigins) {

  public WebSocketProperties {
    allowedOrigins = List.copyOf(allowedOrigins);
  }

  @Override
  public List<String> allowedOrigins() {
    return List.copyOf(allowedOrigins);
  }
}
