package io.point3.p3api.store.infrastructure.external.juso;

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

class JusoSearchAdapterTest {

  private final HttpClient httpClient = mock(HttpClient.class);
  private final JusoSearchAdapter adapter = new JusoSearchAdapter(
      new JusoSearchProperties(
          "https://juso.example.test/address", "test-api-key", 10, Duration.ofSeconds(1)),
      new ObjectMapper(),
      httpClient);

  @Test
  void sendsJusoSearchParametersAndMapsResponse() throws Exception {
    stubResponse(200, """
        {
          "results": {
            "common": {"errorCode": "0"},
            "juso": [{
              "bdNm": "강남역센트럴푸르지오시티",
              "roadAddr": "서울특별시 강남구 테헤란로 123",
              "jibunAddr": "서울특별시 강남구 역삼동 123-45",
              "zipNo": "06234"
            }]
          }
        }
        """);

    List<StoreLocationResult> results = adapter.search("테헤란로 123");

    assertEquals(
        List.of(new StoreLocationResult(
            "강남역센트럴푸르지오시티", "서울특별시 강남구 테헤란로 123", "서울특별시 강남구 역삼동 123-45", "06234")),
        results);
    ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
    verify(httpClient).send(captor.capture(), any());
    assertEquals(
        "https://juso.example.test/address?confmKey=test-api-key&currentPage=1&countPerPage=10&keyword=%ED%85%8C%ED%97%A4%EB%9E%80%EB%A1%9C+123&resultType=json",
        captor.getValue().uri().toString());
  }

  @Test
  void returnsEmptyListWhenJusoResultsAreEmpty() throws Exception {
    stubResponse(200, """
        {"results":{"common":{"errorCode":"0"},"juso":[]}}
        """);

    assertEquals(List.of(), adapter.search("없는주소"));
  }

  @Test
  void convertsApprovalKeyErrorToConfigurationFailure() throws Exception {
    stubResponse(200, """
        {"results":{"common":{"errorCode":"E0001"},"juso":[]}}
        """);

    StoreLocationSearchException exception =
        assertThrows(StoreLocationSearchException.class, () -> adapter.search("테헤란로"));

    assertEquals(StoreLocationSearchException.Type.CONFIGURATION, exception.getType());
  }

  @Test
  void rejectsMissingApprovalKeyBeforeRequest() {
    JusoSearchAdapter adapterWithoutKey = new JusoSearchAdapter(
        new JusoSearchProperties(
            "https://juso.example.test/address", "", 10, Duration.ofSeconds(1)),
        new ObjectMapper(),
        httpClient);

    StoreLocationSearchException exception =
        assertThrows(StoreLocationSearchException.class, () -> adapterWithoutKey.search("테헤란로"));

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
