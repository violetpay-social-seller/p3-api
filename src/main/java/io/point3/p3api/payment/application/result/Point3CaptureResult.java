package io.point3.p3api.payment.application.result;

public record Point3CaptureResult(String sessionId, Status status, String failureCode) {

  public enum Status {
    CAPTURED,
    FAILED,
    PROCESSING
  }
}
