package io.point3.p3api.payment.application.result;

public enum PaymentCtaStatus {
  VIEW_REQUIRED,
  PAYABLE,
  RETRY_AVAILABLE,
  PAYMENT_IN_PROGRESS,
  PAYMENT_NEEDS_CONFIRMATION,
  PAID,
  UNAVAILABLE
}
