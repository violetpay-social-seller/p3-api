package io.point3.p3api.store.infrastructure.external.juso;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "p3.juso-search")
public record JusoSearchProperties(
    String baseUrl, String apiKey, int countPerPage, Duration requestTimeout) {

  private static final String DEFAULT_BASE_URL =
      "https://business.juso.go.kr/addrlink/addrLinkApi.do";
  private static final int DEFAULT_COUNT_PER_PAGE = 10;
  private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(3);

  public JusoSearchProperties {
    baseUrl = normalizeBaseUrl(baseUrl);
    apiKey = apiKey == null ? "" : apiKey.trim();
    countPerPage = countPerPage <= 0 ? DEFAULT_COUNT_PER_PAGE : countPerPage;
    requestTimeout = requestTimeout == null ? DEFAULT_REQUEST_TIMEOUT : requestTimeout;
  }

  private static String normalizeBaseUrl(String value) {
    return value == null || value.isBlank() ? DEFAULT_BASE_URL : value.trim();
  }
}
