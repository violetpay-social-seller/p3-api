package io.point3.p3api.payment.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.point3.p3api.payment.domain.type.RefundOutcome;
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
  @DisplayName("환불 실패는 결과와 실패 시각을 기록한다")
  void recordsFailureResultAndFailedAt() {
    Refund refund = Refund.create(
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 30_400, 80, "구매자 요청");
    Instant failedAt = Instant.parse("2026-08-25T00:00:00Z");

    refund.fail(
        RefundOutcome.MANUAL_REQUIRED,
        null,
        "SETTLEMENT_DEADLINE_EXCEEDED",
        "정산 마감일이 지나 환불을 요청할 수 없습니다",
        "{\"paymentSessionId\":\"pymt_sess-test\"}",
        failedAt);

    assertEquals(RefundStatus.FAILED, refund.getStatus());
    assertEquals(RefundOutcome.MANUAL_REQUIRED, refund.getOutcome());
    assertEquals("SETTLEMENT_DEADLINE_EXCEEDED", refund.getFailureCode());
    assertEquals(failedAt, refund.getFailedAt());
  }

  @Test
  @DisplayName("처리 중 환불의 기존 Point3 실패 원인은 다음 조회 응답에 원인이 없어도 보존한다")
  void preservesProcessingFailureCauseWhenNextProviderResultHasNoFailure() {
    Refund refund = Refund.create(
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 30_400, 80, "구매자 요청");
    refund.startProcessing();
    refund.keepProcessing(
        "ref-processing",
        "REFUND_TEMPORARY_UNAVAILABLE",
        "환불 결과가 아직 확정되지 않았습니다.",
        "{\"refundEntryId\":\"ref-processing\"}");

    refund.keepProcessing("ref-processing", null, null, null);

    assertEquals(RefundStatus.PROCESSING, refund.getStatus());
    assertEquals(RefundOutcome.PROCESSING, refund.getOutcome());
    assertEquals("REFUND_TEMPORARY_UNAVAILABLE", refund.getFailureCode());
    assertEquals("환불 결과가 아직 확정되지 않았습니다.", refund.getFailureMessage());
    assertEquals("{\"refundEntryId\":\"ref-processing\"}", refund.getFailureDetails());
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
