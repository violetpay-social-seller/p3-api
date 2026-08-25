package io.point3.p3api.payment.application.port;

public class Point3PaymentException extends RuntimeException {

  private final String failureCode;

  public Point3PaymentException(String failureCode, String message) {
    super(message);
    this.failureCode = failureCode;
  }

  public String getFailureCode() {
    return failureCode;
  }
}
