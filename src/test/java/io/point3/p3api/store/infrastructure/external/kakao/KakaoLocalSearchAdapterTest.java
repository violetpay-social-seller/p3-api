package io.point3.p3api.store.infrastructure.external.kakao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.store.application.location.port.StoreLocationSearchException;
import io.point3.p3api.store.application.location.result.StoreLocationResult;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class KakaoLocalSearchAdapterTest {

  private final HttpClient httpClient = mock(HttpClient.class);
  private final KakaoLocalSearchAdapter adapter = new KakaoLocalSearchAdapter(
      new KakaoLocalProperties("https://kakao.example.test", "rest-api-key", Duration.ofSeconds(1)),
      new ObjectMapper(),
      httpClient);

  @Test
  void mapsKeywordResponseAndSendsAuthorizationHeader() throws Exception {
    stubResponse(200, """
        {
          "documents": [{
            "place_name": "강남역 2호선",
            "road_address_name": "서울 강남구 강남대로 지하 396",
            "address_name": "서울 강남구 역삼동 858",
            "x": "127.027619",
            "y": "37.497952"
          }]
        }
        """);

    List<StoreLocationResult> results = adapter.searchByKeyword("강남역 2호선");

    assertEquals(
        List.of(new StoreLocationResult(
            "강남역 2호선", "서울 강남구 강남대로 지하 396", "서울 강남구 역삼동 858", 37.497952, 127.027619)),
        results);
    ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
    verify(httpClient).send(captor.capture(), any());
    assertEquals(
        "https://kakao.example.test/v2/local/search/keyword.json?query=%EA%B0%95%EB%82%A8%EC%97%AD+2%ED%98%B8%EC%84%A0",
        captor.getValue().uri().toString());
    assertEquals(
        "KakaoAK rest-api-key",
        captor.getValue().headers().firstValue("Authorization").orElseThrow());
  }

  @Test
  void mapsAddressResponseWithBuildingName() throws Exception {
    stubResponse(200, """
        {
          "documents": [{
            "address_name": "서울 강남구 역삼동 858",
            "x": "127.027619",
            "y": "37.497952",
            "address": {"address_name": "서울 강남구 역삼동 858"},
            "road_address": {
              "address_name": "서울 강남구 강남대로 지하 396",
              "building_name": "강남역 2호선"
            }
          }]
        }
        """);

    List<StoreLocationResult> results = adapter.searchByAddress("강남대로 396");

    assertEquals(
        List.of(new StoreLocationResult(
            "강남역 2호선", "서울 강남구 강남대로 지하 396", "서울 강남구 역삼동 858", 37.497952, 127.027619)),
        results);
  }

  @Test
  void convertsNonSuccessResponseToUnavailableFailure() throws Exception {
    stubResponse(429, "{\"msg\":\"quota exceeded\"}");

    StoreLocationSearchException exception =
        assertThrows(StoreLocationSearchException.class, () -> adapter.searchByKeyword("강남역"));

    assertEquals(StoreLocationSearchException.Type.UNAVAILABLE, exception.getType());
  }

  @Test
  void rejectsMissingRestApiKeyBeforeRequest() {
    KakaoLocalSearchAdapter adapterWithoutKey = new KakaoLocalSearchAdapter(
        new KakaoLocalProperties("https://kakao.example.test", "", Duration.ofSeconds(1)),
        new ObjectMapper(),
        httpClient);

    StoreLocationSearchException exception = assertThrows(
        StoreLocationSearchException.class, () -> adapterWithoutKey.searchByKeyword("강남역"));

    assertEquals(StoreLocationSearchException.Type.CONFIGURATION, exception.getType());
    verifyNoInteractions(httpClient);
  }

  @SuppressWarnings("unchecked")
  private void stubResponse(int statusCode, String body) throws Exception {
    HttpResponse<String> response = mock(HttpResponse.class);
    when(response.statusCode()).thenReturn(statusCode);
    when(response.body()).thenReturn(body);
    when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(response);
  }
}
