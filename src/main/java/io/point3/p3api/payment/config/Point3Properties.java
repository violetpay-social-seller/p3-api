package io.point3.p3api.payment.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "p3.point3")
public record Point3Properties(
    String apiBaseUrl,
    String authBaseUrl,
    String paymentOrigin,
    String clientId,
    String apiToken,
    Duration sessionTtl) {

  public Point3Properties {
    apiBaseUrl = normalizeBaseUrl(apiBaseUrl, "https://api.point3.io");
    authBaseUrl = normalizeBaseUrl(authBaseUrl, "https://pay.point3.io");
    paymentOrigin = normalizeBaseUrl(paymentOrigin, "https://pay.point3.io");
    clientId = clientId == null ? "" : clientId;
    apiToken = apiToken == null ? "" : apiToken;
    sessionTtl = sessionTtl == null ? Duration.ofHours(1) : sessionTtl;
  }

  private static String normalizeBaseUrl(String value, String defaultValue) {
    String normalized = value == null || value.isBlank() ? defaultValue : value;
    if (normalized.endsWith("/")) {
      return normalized.substring(0, normalized.length() - 1);
    }
    return normalized;
  }
}
