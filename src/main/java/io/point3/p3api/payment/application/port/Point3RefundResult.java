package io.point3.p3api.payment.application.port;

import io.point3.p3api.payment.domain.type.RefundOutcome;

public record Point3RefundResult(
    RefundOutcome outcome,
    String providerRefundId,
    String failureCode,
    String failureMessage,
    String failureDetails) {

  public static Point3RefundResult completed(String providerRefundId) {
    return new Point3RefundResult(RefundOutcome.COMPLETED, providerRefundId, null, null, null);
  }

  public static Point3RefundResult processing(
      String providerRefundId, String failureCode, String failureMessage, String failureDetails) {
    return new Point3RefundResult(
        RefundOutcome.PROCESSING, providerRefundId, failureCode, failureMessage, failureDetails);
  }

  public static Point3RefundResult retryable(
      String providerRefundId, String failureCode, String failureMessage, String failureDetails) {
    return new Point3RefundResult(
        RefundOutcome.RETRYABLE, providerRefundId, failureCode, failureMessage, failureDetails);
  }

  public static Point3RefundResult manualRequired(
      String providerRefundId, String failureCode, String failureMessage, String failureDetails) {
    return new Point3RefundResult(
        RefundOutcome.MANUAL_REQUIRED,
        providerRefundId,
        failureCode,
        failureMessage,
        failureDetails);
  }

  public static Point3RefundResult failed(
      String providerRefundId, String failureCode, String failureMessage, String failureDetails) {
    return new Point3RefundResult(
        RefundOutcome.FAILED, providerRefundId, failureCode, failureMessage, failureDetails);
  }

  public boolean completed() {
    return outcome == RefundOutcome.COMPLETED;
  }
}
