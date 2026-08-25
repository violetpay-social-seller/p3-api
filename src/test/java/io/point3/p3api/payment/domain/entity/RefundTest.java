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
}
