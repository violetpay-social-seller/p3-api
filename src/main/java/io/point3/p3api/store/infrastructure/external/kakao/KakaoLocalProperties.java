package io.point3.p3api.store.infrastructure.external.kakao;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "p3.kakao-local")
public record KakaoLocalProperties(String baseUrl, String restApiKey, Duration requestTimeout) {

  private static final String DEFAULT_BASE_URL = "https://dapi.kakao.com";
  private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(3);

  public KakaoLocalProperties {
    baseUrl = normalizeBaseUrl(baseUrl);
    restApiKey = restApiKey == null ? "" : restApiKey.trim();
    requestTimeout = requestTimeout == null ? DEFAULT_REQUEST_TIMEOUT : requestTimeout;
  }

  private static String normalizeBaseUrl(String value) {
    String normalized = value == null || value.isBlank() ? DEFAULT_BASE_URL : value.trim();
    if (normalized.endsWith("/")) {
      return normalized.substring(0, normalized.length() - 1);
    }
    return normalized;
  }
}
