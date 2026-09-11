package io.point3.p3api.account.infrastructure.external.kftc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.account.application.port.AccountRealNameVerificationException;
import io.point3.p3api.account.application.port.AccountRealNameVerificationPort;
import io.point3.p3api.account.application.port.AccountRealNameVerificationRequest;
import io.point3.p3api.account.application.port.AccountRealNameVerificationResult;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KftcAccountRealNameVerificationAdapter implements AccountRealNameVerificationPort {

  private static final String REAL_NAME_PATH = "/v2.0/inquiry/real_name";
  private static final String SUCCESS_API_CODE = "A0000";
  private static final String SUCCESS_BANK_CODE = "000";
  private static final DateTimeFormatter TRANSACTION_TIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.of("Asia/Seoul"));

  private final KftcProperties properties;
  private final KftcAccessTokenProvider tokenProvider;
  private final KftcBankTransactionIdGenerator transactionIdGenerator;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;
  private final Clock clock;

  public KftcAccountRealNameVerificationAdapter(
      KftcProperties properties,
      KftcAccessTokenProvider tokenProvider,
      KftcBankTransactionIdGenerator transactionIdGenerator,
      ObjectMapper objectMapper,
      @Qualifier("kftcHttpClient") HttpClient httpClient,
      Clock clock) {
    this.properties = properties;
    this.tokenProvider = tokenProvider;
    this.transactionIdGenerator = transactionIdGenerator;
    this.objectMapper = objectMapper.copy();
    this.httpClient = httpClient;
    this.clock = clock;
  }

  @Override
  public AccountRealNameVerificationResult verify(AccountRealNameVerificationRequest request) {
    String accessToken = tokenProvider.accessToken();
    HttpResponse<String> response = send(inquiryRequest(request, accessToken));
    if (response.statusCode() == 401 || response.statusCode() == 403) {
      log.warn("KFTC real-name inquiry authentication failed. status={}", response.statusCode());
      throw new AccountRealNameVerificationException(
          AccountRealNameVerificationException.Type.AUTHENTICATION);
    }
    if (response.statusCode() != 200) {
      log.warn("KFTC real-name inquiry failed. status={}", response.statusCode());
      throw new AccountRealNameVerificationException(
          AccountRealNameVerificationException.Type.UNAVAILABLE);
    }

    InquiryResponse inquiry = read(response.body());
    validateSuccess(inquiry);
    validateResponseIdentity(request, inquiry);
    return new AccountRealNameVerificationResult(
        inquiry.apiTransactionId(),
        inquiry.bankCode(),
        requireText(inquiry.bankName()),
        inquiry.accountNumber(),
        inquiry.accountHolderName(),
        inquiry.accountType(),
        clock.instant());
  }

  private HttpRequest inquiryRequest(
      AccountRealNameVerificationRequest request, String accessToken) {
    InquiryRequest inquiry = new InquiryRequest(
        transactionIdGenerator.generate(),
        request.bankCode(),
        request.accountNumber(),
        request.accountHolderInfoType(),
        request.accountHolderInfo(),
        TRANSACTION_TIME_FORMAT.format(clock.instant()));
    return HttpRequest.newBuilder(URI.create(properties.apiBaseUrl() + REAL_NAME_PATH))
        .timeout(properties.requestTimeout())
        .header("Authorization", "Bearer " + accessToken)
        .header("Content-Type", "application/json;charset=UTF-8")
        .POST(body(inquiry))
        .build();
  }

  private HttpRequest.BodyPublisher body(InquiryRequest request) {
    try {
      return HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request));
    } catch (IOException exception) {
      throw new AccountRealNameVerificationException(
          AccountRealNameVerificationException.Type.INVALID_RESPONSE, exception);
    }
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

  private InquiryResponse read(String body) {
    try {
      return objectMapper.readValue(body, InquiryResponse.class);
    } catch (IOException exception) {
      throw new AccountRealNameVerificationException(
          AccountRealNameVerificationException.Type.INVALID_RESPONSE, exception);
    }
  }

  private void validateSuccess(InquiryResponse response) {
    if (!SUCCESS_API_CODE.equals(response.responseCode())
        || !SUCCESS_BANK_CODE.equals(response.bankResponseCode())) {
      String providerCode = SUCCESS_API_CODE.equals(response.responseCode())
          ? response.bankResponseCode()
          : response.responseCode();
      throw new AccountRealNameVerificationException(
          AccountRealNameVerificationException.Type.REJECTED, providerCode);
    }
    if (isBlank(response.apiTransactionId())
        || isBlank(response.bankCode())
        || isBlank(response.accountNumber())
        || isBlank(response.accountHolderName())) {
      throw new AccountRealNameVerificationException(
          AccountRealNameVerificationException.Type.INVALID_RESPONSE);
    }
  }

  private void validateResponseIdentity(
      AccountRealNameVerificationRequest request, InquiryResponse response) {
    if (!request.bankCode().equals(response.bankCode())
        || !request.accountNumber().equals(response.accountNumber())) {
      throw new AccountRealNameVerificationException(
          AccountRealNameVerificationException.Type.INVALID_RESPONSE);
    }
    if (!normalizeName(request.accountHolderName())
        .equalsIgnoreCase(normalizeName(response.accountHolderName()))) {
      throw new AccountRealNameVerificationException(
          AccountRealNameVerificationException.Type.HOLDER_MISMATCH);
    }
  }

  private String normalizeName(String value) {
    return value.replaceAll("\\s+", "");
  }

  private String requireText(String value) {
    if (isBlank(value)) {
      throw new AccountRealNameVerificationException(
          AccountRealNameVerificationException.Type.INVALID_RESPONSE);
    }
    return value;
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private record InquiryRequest(
      @JsonProperty("bank_tran_id") String bankTransactionId,
      @JsonProperty("bank_code_std") String bankCode,
      @JsonProperty("account_num") String accountNumber,
      @JsonProperty("account_holder_info_type") String accountHolderInfoType,
      @JsonProperty("account_holder_info") String accountHolderInfo,
      @JsonProperty("tran_dtime") String transactionDateTime) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record InquiryResponse(
      @JsonProperty("api_tran_id") String apiTransactionId,
      @JsonProperty("rsp_code") String responseCode,
      @JsonProperty("bank_rsp_code") String bankResponseCode,
      @JsonProperty("bank_code_std") String bankCode,
      @JsonProperty("bank_name") String bankName,
      @JsonProperty("account_num") String accountNumber,
      @JsonProperty("account_holder_name") String accountHolderName,
      @JsonProperty("account_type") String accountType) {}
}
