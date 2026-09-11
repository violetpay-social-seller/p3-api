package io.point3.p3api.account.infrastructure.external.kftc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.account.application.port.AccountRealNameVerificationException;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KftcAccessTokenProvider {

  private static final String TOKEN_PATH = "/oauth/2.0/token";

  private final KftcProperties properties;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;
  private final Clock clock;

  private CachedToken cachedToken;

  public KftcAccessTokenProvider(
      KftcProperties properties,
      ObjectMapper objectMapper,
      @Qualifier("kftcHttpClient") HttpClient httpClient,
      Clock clock) {
    this.properties = properties;
    this.objectMapper = objectMapper.copy();
    this.httpClient = httpClient;
    this.clock = clock;
  }

  public synchronized String accessToken() {
    Instant now = clock.instant();
    if (cachedToken != null
        && now.plus(properties.tokenRefreshSkew()).isBefore(cachedToken.expiresAt())) {
      return cachedToken.value();
    }

    cachedToken = issue(now);
    return cachedToken.value();
  }

  private CachedToken issue(Instant issuedAt) {
    validateConfiguration();
    HttpResponse<String> response = send(tokenRequest());
    if (response.statusCode() == 401 || response.statusCode() == 403) {
      log.warn("KFTC token authentication failed. status={}", response.statusCode());
      throw new AccountRealNameVerificationException(
          AccountRealNameVerificationException.Type.AUTHENTICATION);
    }
    if (response.statusCode() != 200) {
      log.warn("KFTC token request failed. status={}", response.statusCode());
      throw new AccountRealNameVerificationException(
          AccountRealNameVerificationException.Type.UNAVAILABLE);
    }

    TokenResponse token = read(response.body());
    if (token.accessToken() == null || token.accessToken().isBlank() || token.expiresIn() <= 0) {
      throw new AccountRealNameVerificationException(
          AccountRealNameVerificationException.Type.INVALID_RESPONSE);
    }
    return new CachedToken(token.accessToken(), issuedAt.plusSeconds(token.expiresIn()));
  }

  private void validateConfiguration() {
    try {
      properties.validateCredentials();
    } catch (KftcConfigurationException exception) {
      throw new AccountRealNameVerificationException(
          AccountRealNameVerificationException.Type.CONFIGURATION, exception);
    }
  }

  private HttpRequest tokenRequest() {
    return HttpRequest.newBuilder(URI.create(properties.authBaseUrl() + TOKEN_PATH))
        .timeout(properties.requestTimeout())
        .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
        .POST(HttpRequest.BodyPublishers.ofString(tokenParameters()))
        .build();
  }

  private String tokenParameters() {
    return "client_id=" + encode(properties.clientId())
        + "&client_secret=" + encode(properties.clientSecret())
        + "&scope=oob"
        + "&grant_type=client_credentials";
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  private HttpResponse<String> send(HttpRequest request) {
    try {
      return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new AccountRealNameVerificationException(
          AccountRealNameVerificationException.Type.UNAVAILABLE, exception);
    } catch (IOException exception) {
      throw new AccountRealNameVerificationException(
          AccountRealNameVerificationException.Type.UNAVAILABLE, exception);
    }
  }

  private TokenResponse read(String body) {
    try {
      return objectMapper.readValue(body, TokenResponse.class);
    } catch (IOException exception) {
      throw new AccountRealNameVerificationException(
          AccountRealNameVerificationException.Type.INVALID_RESPONSE, exception);
    }
  }

  private record CachedToken(String value, Instant expiresAt) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record TokenResponse(
      @JsonProperty("access_token") String accessToken,
      @JsonProperty("expires_in") long expiresIn) {}
}
