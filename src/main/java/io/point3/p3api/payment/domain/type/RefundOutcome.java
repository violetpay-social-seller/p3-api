package io.point3.p3api.payment.domain.type;

public enum RefundOutcome {
  COMPLETED,
  PROCESSING,
  RETRYABLE,
  MANUAL_REQUIRED,
  FAILED
}
