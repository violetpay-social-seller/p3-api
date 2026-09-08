package io.point3.p3api.payment.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.point3.p3api.payment.domain.type.RefundStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RefundTest {

  @Test
  @DisplayName("환불은 요청 상태로 생성되고 완료 시각을 기록한다")
  void completesRefund() {
    Refund refund =
        Refund.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 38000, "구매자 요청");
    Instant completedAt = Instant.parse("2026-08-25T00:00:00Z");

    refund.complete(completedAt);

    assertEquals(100, refund.getRefundRate());
    assertEquals(RefundStatus.COMPLETED, refund.getStatus());
    assertEquals(completedAt, refund.getCompletedAt());
  }

  @Test
  @DisplayName("음수 금액의 환불은 생성할 수 없다")
  void rejectsNegativeAmount() {
    assertThrows(
        IllegalArgumentException.class,
        () -> Refund.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), -1, null));
  }

  @Test
  @DisplayName("정책 환불률을 기록하고 처리 상태로 전환한다")
  void recordsRefundRateAndStartsProcessing() {
    Refund refund = Refund.create(
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 30_400, 80, "구매자 요청");

    refund.startProcessing();

    assertEquals(80, refund.getRefundRate());
    assertEquals(RefundStatus.PROCESSING, refund.getStatus());
  }

  @Test
  @DisplayName("유효 범위를 벗어난 환불률은 기록할 수 없다")
  void rejectsInvalidRefundRate() {
    assertThrows(
        IllegalArgumentException.class,
        () -> Refund.create(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 38_000, 101, null));
  }
}
