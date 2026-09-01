package io.point3.p3api.payment.infrastructure.external.point3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.point3.p3api.payment.application.result.Point3CaptureResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class Point3PaymentAdapterTest {

  @ParameterizedTest
  @CsvSource({
    "captured, CAPTURED",
    "failed, FAILED",
    "expired, FAILED",
    "processing, PROCESSING",
    "created, PROCESSING"
  })
  void mapsPoint3SessionStatus(String point3Status, Point3CaptureResult.Status expectedStatus) {
    assertEquals(expectedStatus, Point3PaymentAdapter.toCaptureStatus(point3Status));
  }
}
