package io.point3.p3api.order.controller.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.point3.p3api.inquiry.domain.type.OrderFormReferenceAssetSource;
import io.point3.p3api.order.application.result.OrderCalendarDayResult;
import io.point3.p3api.order.application.result.OrderCalendarOrderResult;
import io.point3.p3api.order.application.result.OrderCalendarResult;
import io.point3.p3api.order.application.result.OrderReferenceAssetResult;
import io.point3.p3api.order.application.result.OrderResult;
import io.point3.p3api.order.domain.type.OrderStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderReferenceAssetResponseTest {

  @Test
  @DisplayName("주문 목록 응답은 스냅샷 assetId와 delivery 상세를 함께 내려준다")
  void mapsReferenceAssetsToListItem() {
    OrderListItemResponse response = OrderListItemResponse.from(orderResult());

    assertReferenceAssets(response.startReferenceAssets(), response.referenceAssets());
  }

  @Test
  @DisplayName("주문 상세 응답은 스냅샷 assetId와 delivery 상세를 함께 내려준다")
  void mapsReferenceAssetsToDetail() {
    OrderResponse response = OrderResponse.from(orderResult());

    assertReferenceAssets(response.startReferenceAssets(), response.referenceAssets());
  }

  @Test
  @DisplayName("주문 캘린더 응답은 스냅샷 assetId와 delivery 상세를 함께 내려준다")
  void mapsReferenceAssetsToCalendarItem() {
    UUID assetId = UUID.fromString("11111111-1111-4111-8111-111111111111");
    OrderCalendarOrderResult order = new OrderCalendarOrderResult(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        "P3-20260908-0001",
        "케이크",
        39000,
        Instant.parse("2026-09-08T03:00:00Z"),
        LocalDate.of(2026, 9, 8),
        LocalTime.NOON,
        OrderStatus.PAID,
        List.of(referenceAsset(assetId)));
    OrderCalendarResponse response = OrderCalendarResponse.from(OrderCalendarResult.of(
        LocalDate.of(2026, 9, 8),
        LocalDate.of(2026, 9, 8),
        OrderStatus.PAID,
        List.of(OrderCalendarDayResult.of(LocalDate.of(2026, 9, 8), List.of(order)))));

    OrderCalendarResponse.OrderItem item = response.days().getFirst().orders().getFirst();
    assertReferenceAssets(item.startReferenceAssets(), item.referenceAssets());
  }

  private OrderResult orderResult() {
    UUID assetId = UUID.fromString("11111111-1111-4111-8111-111111111111");
    Instant now = Instant.parse("2026-09-08T03:00:00Z");
    return new OrderResult(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        "P3-20260908-0001",
        "케이크",
        "2호 / 초코",
        List.of(referenceAsset(assetId)),
        39000,
        now,
        OrderStatus.PAID,
        null,
        null,
        now,
        now);
  }

  private OrderReferenceAssetResult referenceAsset(UUID assetId) {
    return new OrderReferenceAssetResult(
        assetId,
        OrderFormReferenceAssetSource.USER_UPLOAD,
        0,
        "READY",
        "https://assets.example.test/processed/reference_640.webp",
        List.of(new OrderReferenceAssetResult.Variant(
            "MEDIUM", "https://assets.example.test/processed/reference_640.webp", 640, 480)));
  }

  private void assertReferenceAssets(
      List<UUID> startReferenceAssets, List<ReferenceAssetResponse> referenceAssets) {
    UUID assetId = UUID.fromString("11111111-1111-4111-8111-111111111111");

    assertEquals(List.of(assetId), startReferenceAssets);
    assertEquals(1, referenceAssets.size());
    assertEquals(assetId, referenceAssets.getFirst().assetId());
    assertEquals(OrderFormReferenceAssetSource.USER_UPLOAD, referenceAssets.getFirst().source());
    assertEquals("READY", referenceAssets.getFirst().status());
    assertEquals(
        "https://assets.example.test/processed/reference_640.webp",
        referenceAssets.getFirst().deliveryUrl());
    assertEquals(1, referenceAssets.getFirst().variants().size());
  }
}
