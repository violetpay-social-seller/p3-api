package io.point3.p3api.payment.domain.type;

public enum PaymentAttemptStatus {
  READY,
  IN_PROGRESS,
  SUCCEEDED,
  FAILED,
  NEEDS_CONFIRMATION,
  CANCELED
}
