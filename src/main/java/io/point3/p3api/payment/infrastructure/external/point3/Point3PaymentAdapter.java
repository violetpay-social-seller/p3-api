package io.point3.p3api.payment.infrastructure.external.point3;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.payment.application.port.Point3PaymentException;
import io.point3.p3api.payment.application.port.Point3PaymentPort;
import io.point3.p3api.payment.application.port.Point3RefundResult;
import io.point3.p3api.payment.application.result.Point3CaptureResult;
import io.point3.p3api.payment.application.result.Point3PaymentSession;
import io.point3.p3api.payment.config.Point3Properties;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Point3PaymentAdapter implements Point3PaymentPort {

  private final Point3Properties point3Properties;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient = HttpClient.newHttpClient();

  @Override
  public Point3PaymentSession createSession(
      long amount, String productName, String displayMerchantName) {
    CreatePaymentSessionRequest request =
        new CreatePaymentSessionRequest(amount, productName, displayMerchantName);
    HttpResponse<String> response =
        send(post("/payment/v3/session").POST(body(request)).build(), "POINT3_SESSION_CREATE");

    if (response.statusCode() != 200) {
      throw new Point3PaymentException(
          "POINT3_SESSION_CREATE_" + response.statusCode(), "Point3 session creation failed");
    }

    CreatePaymentSessionResponse session =
        read(response.body(), CreatePaymentSessionResponse.class, "POINT3_SESSION_CREATE_PARSE");
    return new Point3PaymentSession(session.id(), session.amount());
  }

  @Override
  public Point3CaptureResult capture(String sessionId) {
    HttpResponse<String> response = send(
        post("/capture/v2/" + sessionId)
            .POST(HttpRequest.BodyPublishers.noBody())
            .build(),
        "POINT3_CAPTURE");

    if (response.statusCode() == 202) {
      return new Point3CaptureResult(
          sessionId, Point3CaptureResult.Status.PROCESSING, "POINT3_PROCESSING");
    }

    if (response.statusCode() != 200) {
      throw new Point3PaymentException(
          "POINT3_CAPTURE_" + response.statusCode(), "Point3 capture failed");
    }

    CapturePaymentResponse capture =
        read(response.body(), CapturePaymentResponse.class, "POINT3_CAPTURE_PARSE");
    Point3CaptureResult.Status status =
        switch (capture.status()) {
          case "captured" -> Point3CaptureResult.Status.CAPTURED;
          case "failed" -> Point3CaptureResult.Status.FAILED;
          default -> Point3CaptureResult.Status.PROCESSING;
        };
    String failureCode =
        capture.outcome() == null ? capture.status() : capture.outcome().code();

    return new Point3CaptureResult(capture.id(), status, failureCode);
  }

  @Override
  public Point3RefundResult refund(String sessionId, long amount, String reason, String idempotencyKey) {
    RefundRequest refund = new RefundRequest(amount, 0, amount * 10 / 110, reason);
    HttpResponse<String> response = send(post("/refunds/v1/" + sessionId)
        .header("Idempotency-Key", idempotencyKey)
        .POST(body(refund))
        .build(), "POINT3_REFUND");
    if (response.statusCode() != 200) {
      return new Point3RefundResult(false, "POINT3_REFUND_" + response.statusCode());
    }
    RefundResponse result = read(response.body(), RefundResponse.class, "POINT3_REFUND_PARSE");
    return new Point3RefundResult("completed".equals(result.status()), result.status());
  }

  private HttpRequest.Builder post(String path) {
    return HttpRequest.newBuilder(URI.create(point3Properties.apiBaseUrl() + path))
        .header("Authorization", "Bearer " + point3Properties.apiToken())
        .header("Content-Type", "application/json");
  }

  private HttpRequest.BodyPublisher body(Object body) {
    try {
      return HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body));
    } catch (IOException e) {
      throw new Point3PaymentException("POINT3_REQUEST_SERIALIZE", e.getMessage());
    }
  }

  private HttpResponse<String> send(HttpRequest request, String failureCode) {
    try {
      return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new Point3PaymentException(failureCode, e.getMessage());
    } catch (IOException e) {
      throw new Point3PaymentException(failureCode, e.getMessage());
    }
  }

  private <T> T read(String body, Class<T> type, String failureCode) {
    try {
      return objectMapper.readValue(body, type);
    } catch (IOException e) {
      throw new Point3PaymentException(failureCode, e.getMessage());
    }
  }

  private record CreatePaymentSessionRequest(
      long amount, String productName, String displayMerchantName) {}

  private record CreatePaymentSessionResponse(String id, long amount) {}

  private record CapturePaymentResponse(String id, String status, CaptureOutcome outcome) {}

  private record CaptureOutcome(String code) {}

  private record RefundRequest(long refundAmount, long refundTaxFreeAmount, long refundVat, String reason) {}

  private record RefundResponse(String status) {}
}
