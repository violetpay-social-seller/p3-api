package io.point3.p3api.store.infrastructure.external.kakao;

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
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoLocalSearchAdapter implements StoreLocationSearchPort {

  private static final String KEYWORD_SEARCH_PATH = "/v2/local/search/keyword.json";
  private static final String ADDRESS_SEARCH_PATH = "/v2/local/search/address.json";

  private final KakaoLocalProperties properties;
  private final ObjectMapper objectMapper;
  private final HttpClient kakaoLocalHttpClient;

  @Override
  public List<StoreLocationResult> searchByKeyword(String query) {
    KeywordSearchResponse response = get(KEYWORD_SEARCH_PATH, query, KeywordSearchResponse.class);
    return response.documents().stream()
        .map(document -> new StoreLocationResult(
            document.placeName(),
            blankToNull(document.roadAddressName()),
            blankToNull(document.addressName()),
            parseCoordinate(document.y()),
            parseCoordinate(document.x())))
        .toList();
  }

  @Override
  public List<StoreLocationResult> searchByAddress(String query) {
    AddressSearchResponse response = get(ADDRESS_SEARCH_PATH, query, AddressSearchResponse.class);
    return response.documents().stream()
        .map(document -> new StoreLocationResult(
            firstNonBlank(
                document.roadAddress() == null ? null : document.roadAddress().buildingName(),
                document.addressName()),
            document.roadAddress() == null
                ? null
                : blankToNull(document.roadAddress().addressName()),
            document.address() == null ? null : blankToNull(document.address().addressName()),
            parseCoordinate(document.y()),
            parseCoordinate(document.x())))
        .toList();
  }

  private <T> T get(String path, String query, Class<T> responseType) {
    validateConfiguration();
    HttpRequest request = HttpRequest.newBuilder(uri(path, query))
        .timeout(properties.requestTimeout())
        .header("Authorization", "KakaoAK " + properties.restApiKey())
        .GET()
        .build();
    HttpResponse<String> response = send(request);
    if (response.statusCode() != 200) {
      throw new StoreLocationSearchException(StoreLocationSearchException.Type.UNAVAILABLE);
    }
    return read(response.body(), responseType);
  }

  private void validateConfiguration() {
    if (properties.restApiKey().isBlank()
        || properties.requestTimeout().isNegative()
        || properties.requestTimeout().isZero()) {
      throw new StoreLocationSearchException(StoreLocationSearchException.Type.CONFIGURATION);
    }
  }

  private URI uri(String path, String query) {
    String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
    return URI.create(properties.baseUrl() + path + "?query=" + encodedQuery);
  }

  private HttpResponse<String> send(HttpRequest request) {
    try {
      return kakaoLocalHttpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new StoreLocationSearchException(
          StoreLocationSearchException.Type.UNAVAILABLE, exception);
    } catch (IOException exception) {
      throw new StoreLocationSearchException(
          StoreLocationSearchException.Type.UNAVAILABLE, exception);
    }
  }

  private <T> T read(String body, Class<T> responseType) {
    try {
      return objectMapper.readValue(body, responseType);
    } catch (IOException exception) {
      throw new StoreLocationSearchException(
          StoreLocationSearchException.Type.UNAVAILABLE, exception);
    }
  }

  private static double parseCoordinate(String value) {
    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException exception) {
      throw new StoreLocationSearchException(
          StoreLocationSearchException.Type.UNAVAILABLE, exception);
    }
  }

  private static String firstNonBlank(String first, String second) {
    String firstValue = blankToNull(first);
    return firstValue == null ? blankToNull(second) : firstValue;
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }

  private record KeywordSearchResponse(List<KeywordDocument> documents) {}

  private record KeywordDocument(
      @JsonProperty("place_name") String placeName,
      @JsonProperty("road_address_name") String roadAddressName,
      @JsonProperty("address_name") String addressName,
      String x,
      String y) {}

  private record AddressSearchResponse(List<AddressDocument> documents) {}

  private record AddressDocument(
      @JsonProperty("address_name") String addressName,
      String x,
      String y,
      Address address,
      @JsonProperty("road_address") RoadAddress roadAddress) {}

  private record Address(@JsonProperty("address_name") String addressName) {}

  private record RoadAddress(
      @JsonProperty("address_name") String addressName,
      @JsonProperty("building_name") String buildingName) {}
}
