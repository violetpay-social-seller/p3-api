package io.point3.p3api.payment.application.capture;

import java.util.UUID;

public record CapturePaymentCommand(
    UUID paymentAttemptId, UUID buyerUserId, String sessionId, String payerId) {

  public static CapturePaymentCommand of(
      UUID paymentAttemptId, UUID buyerUserId, String sessionId, String payerId) {
    return new CapturePaymentCommand(paymentAttemptId, buyerUserId, sessionId, payerId);
  }
}
