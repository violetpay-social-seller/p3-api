package io.point3.p3api.store.infrastructure.external.juso;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.store.application.location.port.StoreLocationSearchException;
import io.point3.p3api.store.application.location.port.StoreLocationSearchPort;
import io.point3.p3api.store.application.location.result.StoreLocationResult;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JusoSearchAdapter implements StoreLocationSearchPort {

  private static final int MAX_LOG_RESPONSE_LENGTH = 1_000;

  private final JusoSearchProperties properties;
  private final ObjectMapper objectMapper;
  private final HttpClient jusoSearchHttpClient;

  @Override
  public List<StoreLocationResult> search(String query) {
    validateConfiguration();
    HttpResponse<String> response = send(request(query));
    if (response.statusCode() != 200) {
      log.warn(
          "Juso search HTTP error. status={}, responseBody={}",
          response.statusCode(),
          logResponse(response.body()));
      throw new StoreLocationSearchException(StoreLocationSearchException.Type.UNAVAILABLE);
    }

    JusoSearchResponse searchResponse = read(response.body());
    if (searchResponse.results() == null || searchResponse.results().common() == null) {
      log.warn(
          "Juso search response is missing results.common. responseBody={}",
          logResponse(response.body()));
      throw new StoreLocationSearchException(StoreLocationSearchException.Type.UNAVAILABLE);
    }

    String errorCode = searchResponse.results().common().errorCode();
    if (!"0".equals(errorCode)) {
      log.warn(
          "Juso search API error. errorCode={}, errorMessage={}, responseBody={}",
          errorCode,
          searchResponse.results().common().errorMessage(),
          logResponse(response.body()));
      throw new StoreLocationSearchException(errorType(errorCode));
    }

    List<Juso> jusoItems = searchResponse.results().juso() == null
        ? List.of()
        : searchResponse.results().juso();
    return jusoItems.stream()
        .map(juso -> new StoreLocationResult(
            blankToNull(juso.buildingName()),
            blankToNull(juso.roadAddress()),
            blankToNull(juso.jibunAddress()),
            blankToNull(juso.zipCode())))
        .toList();
  }

  private void validateConfiguration() {
    if (properties.apiKey().isBlank()
        || properties.requestTimeout().isNegative()
        || properties.requestTimeout().isZero()) {
      log.warn(
          "Juso search configuration invalid. apiKeyConfigured={}, requestTimeout={}",
          !properties.apiKey().isBlank(),
          properties.requestTimeout());
      throw new StoreLocationSearchException(StoreLocationSearchException.Type.CONFIGURATION);
    }
  }

  private HttpRequest request(String query) {
    return HttpRequest.newBuilder(uri(query))
        .timeout(properties.requestTimeout())
        .GET()
        .build();
  }

  private URI uri(String query) {
    return URI.create(properties.baseUrl() + "?" + parameters(query));
  }

  private String parameters(String query) {
    return "confmKey=" + encode(properties.apiKey())
        + "&currentPage=1"
        + "&countPerPage=" + properties.countPerPage()
        + "&keyword=" + encode(query)
        + "&resultType=json";
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  private HttpResponse<String> send(HttpRequest request) {
    try {
      return jusoSearchHttpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new StoreLocationSearchException(
          StoreLocationSearchException.Type.UNAVAILABLE, exception);
    } catch (IOException exception) {
      log.warn("Juso search HTTP request failed.", exception);
      throw new StoreLocationSearchException(
          StoreLocationSearchException.Type.UNAVAILABLE, exception);
    }
  }

  private String logResponse(String responseBody) {
    if (responseBody == null || responseBody.length() <= MAX_LOG_RESPONSE_LENGTH) {
      return responseBody;
    }
    return responseBody.substring(0, MAX_LOG_RESPONSE_LENGTH) + "...";
  }

  private JusoSearchResponse read(String body) {
    try {
      return objectMapper.readValue(body, JusoSearchResponse.class);
    } catch (IOException exception) {
      log.warn(
          "Juso search response parsing failed. responseBody={}", logResponse(body), exception);
      throw new StoreLocationSearchException(
          StoreLocationSearchException.Type.UNAVAILABLE, exception);
    }
  }

  private StoreLocationSearchException.Type errorType(String errorCode) {
    return errorCode != null && errorCode.startsWith("E000")
        ? StoreLocationSearchException.Type.CONFIGURATION
        : StoreLocationSearchException.Type.UNAVAILABLE;
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }

  private record JusoSearchResponse(Results results) {}

  private record Results(Common common, List<Juso> juso) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record Common(
      @JsonProperty("errorCode") String errorCode,
      @JsonProperty("errorMessage") String errorMessage) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record Juso(
      @JsonProperty("bdNm") String buildingName,
      @JsonProperty("roadAddr") String roadAddress,
      @JsonProperty("jibunAddr") String jibunAddress,
      @JsonProperty("zipNo") String zipCode) {}
}
