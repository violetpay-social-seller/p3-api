package io.point3.p3api.payment.application.port;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class Point3RefundStatusResultTest {

  @Test
  @DisplayName("provider refund id가 없고 Point3 환불 엔트리가 여러 개면 특정 환불로 매칭하지 않는다")
  void doesNotMatchWhenProviderIdIsMissingAndRefundsAreAmbiguous() {
    Point3RefundStatusResult result = new Point3RefundStatusResult(
        "pymt_sess-test",
        "processing",
        0,
        false,
        List.of(
            Point3RefundResult.completed("ref-first"),
            Point3RefundResult.completed("ref-second")));

    assertTrue(result.findRefund(null).isEmpty());
    assertTrue(result.hasAmbiguousRefundsWithoutProviderId(null));
  }

  @Test
  @DisplayName("provider refund id가 있으면 동일한 Point3 환불 엔트리만 매칭한다")
  void matchesOnlyExactProviderRefundId() {
    Point3RefundStatusResult result = new Point3RefundStatusResult(
        "pymt_sess-test",
        "processing",
        0,
        false,
        List.of(
            Point3RefundResult.completed("ref-first"),
            Point3RefundResult.completed("ref-second")));

    assertEquals("ref-second", result.findRefund("ref-second").orElseThrow().providerRefundId());
    assertTrue(result.findRefund("ref-missing").isEmpty());
  }
}
