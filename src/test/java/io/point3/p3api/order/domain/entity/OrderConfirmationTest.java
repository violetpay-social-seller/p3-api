package io.point3.p3api.order.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.point3.p3api.order.domain.type.OrderConfirmationStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderConfirmationTest {

  @Test
  @DisplayName("주문확인서는 DRAFT로 생성된 뒤 전송, 수정요청, 대체 상태로 전이된다")
  void changesStatusThroughConfirmationLifecycle() {
    OrderConfirmation confirmation = confirmation();
    Instant sentAt = Instant.parse("2026-08-25T00:00:00Z");
    Instant revisionRequestedAt = Instant.parse("2026-08-25T01:00:00Z");
    UUID replacementId = UUID.randomUUID();

    confirmation.sent(sentAt);
    confirmation.requestRevision(revisionRequestedAt);
    confirmation.replaceWith(replacementId);

    assertEquals(OrderConfirmationStatus.REPLACED, confirmation.getStatus());
    assertEquals(sentAt, confirmation.getSentAt());
    assertEquals(revisionRequestedAt, confirmation.getRevisionRequestedAt());
    assertEquals(replacementId, confirmation.getReplacedByConfirmationId());
  }

  @Test
  @DisplayName("구매자 확인 시각은 최초 확인 시각만 보존한다")
  void keepsFirstBuyerViewedAt() {
    OrderConfirmation confirmation = confirmation();
    Instant firstViewedAt = Instant.parse("2026-08-25T00:00:00Z");
    Instant secondViewedAt = Instant.parse("2026-08-25T01:00:00Z");

    confirmation.markBuyerViewed(firstViewedAt);
    confirmation.markBuyerViewed(secondViewedAt);

    assertEquals(firstViewedAt, confirmation.getBuyerViewedAt());
  }

  @Test
  @DisplayName("음수 금액의 주문확인서는 생성할 수 없다")
  void rejectsNegativeAmount() {
    assertThrows(
        IllegalArgumentException.class,
        () -> OrderConfirmation.create(
            UUID.randomUUID(),
            null,
            UUID.randomUUID(),
            "초코 케이크",
            "초코 시트",
            -1,
            Instant.parse("2026-08-30T04:30:00Z"),
            "P3 베이커리",
            null,
            null,
            null));
  }

  private OrderConfirmation confirmation() {
    return OrderConfirmation.create(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        "초코 케이크",
        "초코 시트",
        38000,
        Instant.parse("2026-08-30T04:30:00Z"),
        "P3 베이커리",
        null,
        null,
        null);
  }
}
