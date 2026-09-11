package io.point3.p3api.account.infrastructure.external.kftc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.account.application.port.AccountRealNameVerificationException;
import io.point3.p3api.account.application.port.AccountRealNameVerificationRequest;
import io.point3.p3api.account.application.port.AccountRealNameVerificationResult;
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

class KftcAccountRealNameVerificationAdapterTest {

  private static final Instant NOW = Instant.parse("2026-09-11T02:00:00Z");

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final HttpClient httpClient = mock(HttpClient.class);
  private final KftcAccessTokenProvider tokenProvider = mock(KftcAccessTokenProvider.class);
  private final KftcBankTransactionIdGenerator transactionIdGenerator =
      mock(KftcBankTransactionIdGenerator.class);
  private KftcAccountRealNameVerificationAdapter adapter;

  @BeforeEach
  void setUp() {
    KftcProperties properties = new KftcProperties(
        "https://auth.example.test",
        "https://api.example.test",
        "client-id",
        "client-secret",
        "F123456789",
        Duration.ofSeconds(1),
        Duration.ofSeconds(30));
    adapter = new KftcAccountRealNameVerificationAdapter(
        properties,
        tokenProvider,
        transactionIdGenerator,
        objectMapper,
        httpClient,
        Clock.fixed(NOW, ZoneOffset.UTC));
    when(tokenProvider.accessToken()).thenReturn("access-token");
    when(transactionIdGenerator.generate()).thenReturn("F123456789U123456789");
  }

  @Test
  @DisplayName("계좌실명조회 요청을 전송하고 검증된 예금주 정보를 반환한다")
  void verifiesAccountHolder() throws Exception {
    stubResponse(200, successResponse("홍 길동"));

    AccountRealNameVerificationResult result = adapter.verify(request("홍길동"));

    assertEquals("api-transaction-id", result.providerTransactionId());
    assertEquals("국민은행", result.bankName());
    assertEquals("홍 길동", result.accountHolderName());
    assertEquals(NOW, result.verifiedAt());

    ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
    verify(httpClient).send(captor.capture(), any());
    HttpRequest httpRequest = captor.getValue();
    assertEquals(
        "https://api.example.test/v2.0/inquiry/real_name", httpRequest.uri().toString());
    assertEquals(
        "Bearer access-token", httpRequest.headers().firstValue("Authorization").orElseThrow());
    JsonNode requestBody = objectMapper.readTree(body(httpRequest));
    assertEquals("F123456789U123456789", requestBody.path("bank_tran_id").asText());
    assertEquals("004", requestBody.path("bank_code_std").asText());
    assertEquals("123456789012", requestBody.path("account_num").asText());
    assertEquals("20260911110000", requestBody.path("tran_dtime").asText());
  }

  @Test
  @DisplayName("입력한 예금주와 조회된 예금주가 다르면 검증에 실패한다")
  void rejectsHolderMismatch() throws Exception {
    stubResponse(200, successResponse("김철수"));

    AccountRealNameVerificationException exception = assertThrows(
        AccountRealNameVerificationException.class, () -> adapter.verify(request("홍길동")));

    assertEquals(AccountRealNameVerificationException.Type.HOLDER_MISMATCH, exception.getType());
  }

  @Test
  @DisplayName("금융결제원 실패 응답 코드를 거절 결과로 변환한다")
  void convertsProviderRejection() throws Exception {
    stubResponse(200, """
        {"rsp_code":"A0001","rsp_message":"실패","bank_rsp_code":"999"}
        """);

    AccountRealNameVerificationException exception = assertThrows(
        AccountRealNameVerificationException.class, () -> adapter.verify(request("홍길동")));

    assertEquals(AccountRealNameVerificationException.Type.REJECTED, exception.getType());
    assertEquals("A0001", exception.getProviderCode());
  }

  @Test
  @DisplayName("인증 HTTP 오류를 재시도하지 않고 인증 예외로 변환한다")
  void convertsHttpAuthenticationFailure() throws Exception {
    stubResponse(401, "{}");

    AccountRealNameVerificationException exception = assertThrows(
        AccountRealNameVerificationException.class, () -> adapter.verify(request("홍길동")));

    assertEquals(AccountRealNameVerificationException.Type.AUTHENTICATION, exception.getType());
    verify(httpClient).send(any(HttpRequest.class), any());
  }

  private AccountRealNameVerificationRequest request(String holderName) {
    return new AccountRealNameVerificationRequest("004", "123456789012", holderName, " ", "900101");
  }

  private String successResponse(String holderName) {
    return """
        {
          "api_tran_id":"api-transaction-id",
          "rsp_code":"A0000",
          "bank_rsp_code":"000",
          "bank_code_std":"004",
          "bank_name":"국민은행",
          "account_num":"123456789012",
          "account_holder_name":"${holderName}",
          "account_type":"1"
        }
        """.replace("${holderName}", holderName);
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
