package io.point3.p3api.account.infrastructure.external.kftc;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "p3.kftc")
public record KftcProperties(
    String authBaseUrl,
    String apiBaseUrl,
    String clientId,
    String clientSecret,
    String useOrgCode,
    Duration requestTimeout,
    Duration tokenRefreshSkew) {

  private static final String DEFAULT_BASE_URL = "https://openapi.openbanking.or.kr";
  private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(3);
  private static final Duration DEFAULT_TOKEN_REFRESH_SKEW = Duration.ofSeconds(30);

  public KftcProperties {
    authBaseUrl = normalizeBaseUrl(authBaseUrl);
    apiBaseUrl = normalizeBaseUrl(apiBaseUrl);
    clientId = normalizeSecret(clientId);
    clientSecret = normalizeSecret(clientSecret);
    useOrgCode = normalizeSecret(useOrgCode);
    requestTimeout = requestTimeout == null ? DEFAULT_REQUEST_TIMEOUT : requestTimeout;
    tokenRefreshSkew = tokenRefreshSkew == null ? DEFAULT_TOKEN_REFRESH_SKEW : tokenRefreshSkew;
  }

  public void validateCredentials() {
    if (clientId.isBlank()
        || clientSecret.isBlank()
        || requestTimeout.isZero()
        || requestTimeout.isNegative()) {
      throw new KftcConfigurationException();
    }
  }

  public void validateAccountInquiry() {
    validateCredentials();
    if (!useOrgCode.matches("[A-Za-z0-9]{10}")) {
      throw new KftcConfigurationException();
    }
  }

  private static String normalizeBaseUrl(String value) {
    String baseUrl = value == null || value.isBlank() ? DEFAULT_BASE_URL : value.trim();
    return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
  }

  private static String normalizeSecret(String value) {
    return value == null ? "" : value.trim();
  }
}
