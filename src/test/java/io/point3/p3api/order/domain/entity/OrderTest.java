package io.point3.p3api.order.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.point3.p3api.order.domain.type.OrderStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderTest {

  @Test
  @DisplayName("주문은 결제 완료 상태와 주문 스냅샷으로 생성된다")
  void createsPaidOrderWithSnapshots() {
    Instant pickupAt = Instant.parse("2026-08-30T04:30:00Z");

    Order order = Order.create(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        "P3-20260830-0001",
        "초코 케이크",
        "초코 시트",
        38000,
        pickupAt);

    assertEquals(OrderStatus.PAID, order.getStatus());
    assertEquals("P3-20260830-0001", order.getOrderNumber());
    assertEquals("초코 케이크", order.getMenuNameSnapshot());
    assertEquals("[]", order.getStartReferenceAssets());
    assertEquals(38000, order.getPaidAmount());
    assertEquals(pickupAt, order.getPickupAt());
  }

  @Test
  @DisplayName("주문은 주문 시작 참조 이미지 스냅샷을 함께 생성할 수 있다")
  void createsPaidOrderWithStartReferenceAssets() {
    UUID assetId = UUID.randomUUID();

    Order order = Order.create(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        "P3-20260830-0001",
        "초코 케이크",
        "초코 시트",
        "[\"" + assetId + "\"]",
        38000,
        Instant.parse("2026-08-30T04:30:00Z"));

    assertEquals("[\"" + assetId + "\"]", order.getStartReferenceAssets());
  }

  @Test
  @DisplayName("음수 결제 금액의 주문은 생성할 수 없다")
  void rejectsNegativePaidAmount() {
    assertThrows(
        IllegalArgumentException.class,
        () -> Order.create(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "P3-20260830-0001",
            "초코 케이크",
            "초코 시트",
            -1,
            Instant.parse("2026-08-30T04:30:00Z")));
  }
}
