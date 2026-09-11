package io.point3.p3api.payment.infrastructure.external.point3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.payment.application.port.Point3PaymentException;
import io.point3.p3api.payment.application.port.Point3PaymentPort;
import io.point3.p3api.payment.application.port.Point3RefundResult;
import io.point3.p3api.payment.application.port.Point3RefundStatusResult;
import io.point3.p3api.payment.application.result.Point3CaptureResult;
import io.point3.p3api.payment.application.result.Point3PaymentSession;
import io.point3.p3api.payment.config.Point3Properties;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Point3PaymentAdapter implements Point3PaymentPort {

  private static final int MAX_FAILURE_BODY_LENGTH = 1_000;

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
          "POINT3_SESSION_CREATE_" + response.statusCode(),
          failureMessage("Point3 session creation failed", response));
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
          "POINT3_CAPTURE_" + response.statusCode(),
          failureMessage("Point3 capture failed", response));
    }

    CapturePaymentResponse capture =
        read(response.body(), CapturePaymentResponse.class, "POINT3_CAPTURE_PARSE");
    return toCaptureResult(capture.id(), capture.status(), capture.outcome());
  }

  @Override
  public Point3CaptureResult getSession(String sessionId) {
    HttpResponse<String> response =
        send(get("/payment/v3/session/" + sessionId).GET().build(), "POINT3_SESSION_GET");

    if (response.statusCode() != 200) {
      throw new Point3PaymentException(
          "POINT3_SESSION_GET_" + response.statusCode(),
          failureMessage("Point3 session lookup failed", response));
    }

    PaymentSessionResponse session =
        read(response.body(), PaymentSessionResponse.class, "POINT3_SESSION_GET_PARSE");
    return toCaptureResult(session.id(), session.status(), session.outcome());
  }

  @Override
  public Point3RefundResult refund(
      String sessionId, long amount, String reason, String idempotencyKey) {
    RefundRequest refund = new RefundRequest(amount, 0, amount * 10 / 110, reason);
    HttpResponse<String> response;
    try {
      response = send(
          post("/refunds/v1/" + sessionId)
              .header("Idempotency-Key", idempotencyKey)
              .POST(body(refund))
              .build(),
          "POINT3_REFUND");
    } catch (Point3PaymentException exception) {
      return Point3RefundResult.processing(
          null, exception.getFailureCode(), exception.getMessage(), null);
    }
    if (response.statusCode() != 200) {
      return toRefundFailureResult(response.statusCode(), response.body());
    }
    RefundResponse result = read(response.body(), RefundResponse.class, "POINT3_REFUND_PARSE");
    return toRefundResult(result.id(), result.status(), null);
  }

  @Override
  public Point3RefundStatusResult getRefundStatus(String sessionId) {
    return getRefundStatus(
        get("/refunds/v1/" + sessionId).GET().build(), "POINT3_REFUND_STATUS_GET", sessionId);
  }

  @Override
  public Point3RefundStatusResult resumeRefund(String sessionId) {
    return getRefundStatus(
        post("/refunds/v1/" + sessionId + "/resume")
            .POST(HttpRequest.BodyPublishers.noBody())
            .build(),
        "POINT3_REFUND_RESUME",
        sessionId);
  }

  private Point3RefundStatusResult getRefundStatus(
      HttpRequest request, String failureCode, String sessionId) {
    HttpResponse<String> response;
    try {
      response = send(request, failureCode);
    } catch (Point3PaymentException exception) {
      return new Point3RefundStatusResult(
          sessionId,
          "processing",
          0,
          false,
          List.of(Point3RefundResult.processing(
              null, exception.getFailureCode(), exception.getMessage(), null)));
    }
    if (response.statusCode() != 200) {
      return new Point3RefundStatusResult(
          sessionId,
          "processing",
          0,
          false,
          List.of(toRefundFailureResult(response.statusCode(), response.body())));
    }

    RefundStatusResponse result =
        read(response.body(), RefundStatusResponse.class, failureCode + "_PARSE");
    List<Point3RefundResult> refunds = result.refunds().stream()
        .map(refund -> toRefundResult(refund.id(), refund.status(), refund.failure()))
        .toList();
    return new Point3RefundStatusResult(
        result.paymentSessionId(),
        result.status(),
        result.refundableAmount(),
        result.canCreateRefund(),
        refunds);
  }

  private HttpRequest.Builder post(String path) {
    return request(path);
  }

  private HttpRequest.Builder get(String path) {
    return request(path);
  }

  private HttpRequest.Builder request(String path) {
    return HttpRequest.newBuilder(URI.create(point3Properties.apiBaseUrl() + path))
        .header("Authorization", "Bearer " + point3Properties.apiToken())
        .header("Content-Type", "application/json");
  }

  private static Point3CaptureResult toCaptureResult(
      String sessionId, String status, CaptureOutcome outcome) {
    String failureCode = outcome == null ? status : outcome.code();
    return new Point3CaptureResult(sessionId, toCaptureStatus(status), failureCode);
  }

  static Point3CaptureResult.Status toCaptureStatus(String status) {
    return switch (status) {
      case "captured" -> Point3CaptureResult.Status.CAPTURED;
      case "failed", "expired" -> Point3CaptureResult.Status.FAILED;
      default -> Point3CaptureResult.Status.PROCESSING;
    };
  }

  static Point3RefundResult toRefundFailureResult(int statusCode, String body) {
    Point3Error error = parsePoint3Error(body);
    String failureCode = error.code() == null ? "POINT3_REFUND_" + statusCode : error.code();
    return classifyRefundFailure(
        statusCode, error.refundEntryId(), failureCode, error.message(), error.details());
  }

  private static Point3RefundResult classifyRefundFailure(
      int statusCode,
      String providerRefundId,
      String failureCode,
      String failureMessage,
      String failureDetails) {
    String code = failureCode == null ? "POINT3_REFUND_" + statusCode : failureCode;
    return switch (code) {
      case "SETTLEMENT_DEADLINE_EXCEEDED" ->
        Point3RefundResult.manualRequired(providerRefundId, code, failureMessage, failureDetails);
      case "EOB_WINDOW_BLOCKED" ->
        Point3RefundResult.retryable(providerRefundId, code, failureMessage, failureDetails);
      case "REFUND_TEMPORARY_UNAVAILABLE" ->
        Point3RefundResult.processing(providerRefundId, code, failureMessage, failureDetails);
      default -> {
        if (statusCode >= 500) {
          yield Point3RefundResult.processing(
              providerRefundId, code, failureMessage, failureDetails);
        }
        yield Point3RefundResult.failed(providerRefundId, code, failureMessage, failureDetails);
      }
    };
  }

  private static Point3RefundResult toRefundResult(
      String providerRefundId, String status, RefundFailure failure) {
    if ("completed".equals(status)) {
      return Point3RefundResult.completed(providerRefundId);
    }
    if ("processing".equals(status)) {
      return Point3RefundResult.processing(
          providerRefundId,
          failure == null ? null : failure.code(),
          failure == null ? null : failure.message(),
          null);
    }
    if (failure != null) {
      return classifyRefundFailure(422, providerRefundId, failure.code(), failure.message(), null);
    }
    return Point3RefundResult.failed(providerRefundId, status, null, null);
  }

  private static Point3Error parsePoint3Error(String body) {
    if (body == null || body.isBlank()) {
      return new Point3Error(null, null, null, null);
    }
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(body);
      JsonNode result = root.path("result");
      String code = text(result.path("code"));
      if (code == null) {
        code = text(root.path("code"));
      }
      String message = text(result.path("message"));
      if (message == null) {
        message = text(root.path("message"));
      }
      JsonNode detailsNode = result.path("details");
      if (detailsNode.isMissingNode() || detailsNode.isNull()) {
        detailsNode = root.path("details");
      }
      String details = detailsNode.isMissingNode() || detailsNode.isNull()
          ? null
          : mapper.writeValueAsString(detailsNode);
      String refundEntryId = text(detailsNode.path("refundEntryId"));
      return new Point3Error(code, message, details, refundEntryId);
    } catch (IOException exception) {
      return new Point3Error(null, body, null, null);
    }
  }

  private static String text(JsonNode node) {
    if (node == null || node.isMissingNode() || node.isNull()) {
      return null;
    }
    return node.asText();
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

  private String failureMessage(String message, HttpResponse<String> response) {
    String body = response.body();
    if (body == null || body.isBlank()) {
      return message + ". statusCode=" + response.statusCode() + " body=<empty>";
    }

    String normalized = body.replaceAll("\\s+", " ").trim();
    String truncated = normalized.length() <= MAX_FAILURE_BODY_LENGTH
        ? normalized
        : normalized.substring(0, MAX_FAILURE_BODY_LENGTH) + "...";
    return message + ". statusCode=" + response.statusCode() + " body=" + truncated;
  }

  private record CreatePaymentSessionRequest(
      long amount, String productName, String displayMerchantName) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record CreatePaymentSessionResponse(String id, long amount) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record CapturePaymentResponse(String id, String status, CaptureOutcome outcome) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record PaymentSessionResponse(String id, String status, CaptureOutcome outcome) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record CaptureOutcome(String code) {}

  private record RefundRequest(
      long refundAmount, long refundTaxFreeAmount, long refundVat, String reason) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record RefundResponse(String id, String status) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record RefundStatusResponse(
      String paymentSessionId,
      String status,
      long refundableAmount,
      boolean canCreateRefund,
      List<RefundEntryResponse> refunds) {
    private RefundStatusResponse {
      refunds = refunds == null ? List.of() : List.copyOf(refunds);
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record RefundEntryResponse(String id, String status, RefundFailure failure) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record RefundFailure(String code, String message) {}

  private record Point3Error(String code, String message, String details, String refundEntryId) {}
}
