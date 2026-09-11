package io.point3.p3api.account.infrastructure.external.kftc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.account.application.port.AccountRealNameVerificationException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.Flow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class KftcAccessTokenProviderTest {

  private final HttpClient httpClient = mock(HttpClient.class);
  private final Clock clock = Clock.fixed(Instant.parse("2026-09-11T02:00:00Z"), ZoneOffset.UTC);
  private final KftcProperties properties = properties("client-id", "client-secret");
  private KftcAccessTokenProvider tokenProvider;

  @BeforeEach
  void setUp() {
    tokenProvider = new KftcAccessTokenProvider(properties, new ObjectMapper(), httpClient, clock);
  }

  @Test
  @DisplayName("Client Credentials와 oob scope로 기관용 토큰을 발급한다")
  void issuesInstitutionToken() throws Exception {
    stubResponse(200, """
        {"access_token":"access-token","token_type":"Bearer","expires_in":"600","scope":"oob"}
        """);

    String accessToken = tokenProvider.accessToken();

    assertEquals("access-token", accessToken);
    ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
    verify(httpClient).send(captor.capture(), any());
    HttpRequest request = captor.getValue();
    assertEquals("https://auth.example.test/oauth/2.0/token", request.uri().toString());
    assertEquals("POST", request.method());
    assertEquals(
        "client_id=client-id&client_secret=client-secret&scope=oob&grant_type=client_credentials",
        body(request));
  }

  @Test
  @DisplayName("유효기간이 남은 기관용 토큰을 재사용한다")
  void cachesTokenUntilRefreshWindow() throws Exception {
    stubResponse(200, """
        {"access_token":"access-token","expires_in":600}
        """);

    assertEquals("access-token", tokenProvider.accessToken());
    assertEquals("access-token", tokenProvider.accessToken());

    verify(httpClient, times(1)).send(any(HttpRequest.class), any());
  }

  @Test
  @DisplayName("Credential이 없으면 금융결제원에 요청하지 않는다")
  void rejectsMissingCredentials() {
    KftcAccessTokenProvider missingCredentials =
        new KftcAccessTokenProvider(properties("", ""), new ObjectMapper(), httpClient, clock);

    AccountRealNameVerificationException exception =
        assertThrows(AccountRealNameVerificationException.class, missingCredentials::accessToken);

    assertEquals(AccountRealNameVerificationException.Type.CONFIGURATION, exception.getType());
    verifyNoInteractions(httpClient);
  }

  @Test
  @DisplayName("금융결제원 인증 실패를 인증 예외로 변환한다")
  void convertsAuthenticationFailure() throws Exception {
    stubResponse(401, """
        {"rsp_code":"O0001","rsp_message":"인증 실패"}
        """);

    AccountRealNameVerificationException exception =
        assertThrows(AccountRealNameVerificationException.class, tokenProvider::accessToken);

    assertEquals(AccountRealNameVerificationException.Type.AUTHENTICATION, exception.getType());
    assertEquals("O0001", exception.getProviderCode());
    assertEquals("인증 실패", exception.getProviderError().responseMessage());
  }

  private KftcProperties properties(String clientId, String clientSecret) {
    return new KftcProperties(
        "https://auth.example.test",
        "https://api.example.test",
        clientId,
        clientSecret,
        "F123456789",
        Duration.ofSeconds(1),
        Duration.ofSeconds(30));
  }

  @SuppressWarnings("unchecked")
  private void stubResponse(int statusCode, String body) throws Exception {
    HttpResponse<String> response = mock(HttpResponse.class);
    when(response.statusCode()).thenReturn(statusCode);
    when(response.body()).thenReturn(body);
    when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(response);
  }

  private String body(HttpRequest request) {
    HttpRequest.BodyPublisher publisher = request.bodyPublisher().orElseThrow();
    StringBuilder body = new StringBuilder();
    publisher.subscribe(new Flow.Subscriber<ByteBuffer>() {
      @Override
      public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
      }

      @Override
      public void onNext(ByteBuffer item) {
        body.append(StandardCharsets.UTF_8.decode(item));
      }

      @Override
      public void onError(Throwable throwable) {
        throw new AssertionError(throwable);
      }

      @Override
      public void onComplete() {}
    });
    return body.toString();
  }
}
