package io.point3.p3api.order.application.refund;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.point3.p3api.store.domain.entity.StoreRefundPolicy;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderRefundPolicyCalculatorTest {

  private static final UUID STORE_ID = UUID.randomUUID();

  private final OrderRefundPolicyCalculator calculator = new OrderRefundPolicyCalculator();

  @Test
  void appliesPolicyAtExactKoreaCalendarDateBoundary() {
    var calculation = calculator.calculate(
        50_000,
        Instant.parse("2030-09-15T04:00:00Z"),
        Instant.parse("2030-09-08T14:59:59Z"),
        policies());

    assertEquals(100, calculation.refundRate());
    assertEquals(50_000, calculation.amount());
  }

  @Test
  void appliesClosestThresholdThatHasNotPassed() {
    var calculation = calculator.calculate(
        50_000,
        Instant.parse("2030-09-15T04:00:00Z"),
        Instant.parse("2030-09-09T15:00:00Z"),
        policies());

    assertEquals(80, calculation.refundRate());
    assertEquals(40_000, calculation.amount());
  }

  @Test
  void usesKoreaCalendarDateInsteadOfElapsedHours() {
    var calculation = calculator.calculate(
        50_000,
        Instant.parse("2030-09-15T00:00:00Z"),
        Instant.parse("2030-09-14T15:30:00Z"),
        List.of(policy(0, 30)));

    assertEquals(30, calculation.refundRate());
    assertEquals(15_000, calculation.amount());
  }

  @Test
  void returnsZeroWhenNoPolicyThresholdMatches() {
    var calculation = calculator.calculate(
        50_000,
        Instant.parse("2030-09-15T04:00:00Z"),
        Instant.parse("2030-09-13T04:00:00Z"),
        policies());

    assertEquals(0, calculation.refundRate());
    assertEquals(0, calculation.amount());
  }

  @Test
  void truncatesSubWonAmountWithoutOverflowingMultiplication() {
    var calculation = calculator.calculate(
        10_001,
        Instant.parse("2030-09-15T04:00:00Z"),
        Instant.parse("2030-09-10T04:00:00Z"),
        policies());

    assertEquals(80, calculation.refundRate());
    assertEquals(8_000, calculation.amount());
  }

  @Test
  void rejectsNegativePaidAmountAndInvalidPolicy() {
    Instant pickupAt = Instant.parse("2030-09-15T04:00:00Z");
    Instant requestedAt = Instant.parse("2030-09-08T04:00:00Z");

    assertThrows(
        IllegalArgumentException.class,
        () -> calculator.calculate(-1, pickupAt, requestedAt, policies()));
    assertThrows(
        IllegalArgumentException.class,
        () -> calculator.calculate(10_000, pickupAt, requestedAt, List.of(policy(7, 101))));
  }

  private List<StoreRefundPolicy> policies() {
    return List.of(policy(7, 100), policy(5, 80), policy(3, 50));
  }

  private StoreRefundPolicy policy(int daysBeforePickup, int refundRate) {
    return StoreRefundPolicy.create(STORE_ID, daysBeforePickup, refundRate, 0);
  }
}
