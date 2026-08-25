package io.point3.p3api.payment.controller.response;

import io.point3.p3api.payment.application.result.PaymentCaptureResult;
import io.point3.p3api.payment.domain.type.PaymentAttemptStatus;
import java.util.UUID;

public record PaymentCaptureResponse(
    UUID paymentAttemptId,
    String sessionId,
    long amount,
    PaymentAttemptStatus status,
    UUID orderId,
    String failureCode) {

  public static PaymentCaptureResponse from(PaymentCaptureResult result) {
    return new PaymentCaptureResponse(
        result.paymentAttemptId(),
        result.sessionId(),
        result.amount(),
        result.status(),
        result.orderId(),
        result.failureCode());
  }
}
