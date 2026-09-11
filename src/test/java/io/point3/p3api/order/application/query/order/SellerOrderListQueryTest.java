package io.point3.p3api.order.application.query.order;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.point3.p3api.order.domain.type.OrderStatus;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SellerOrderListQueryTest {

  @Test
  @DisplayName("기존 환불 관련 주문 상태 필터는 새 상태 모델로 매핑한다")
  void mapsLegacyRefundStatusFilters() {
    SellerOrderListQuery query = SellerOrderListQuery.of(
        UUID.randomUUID(),
        List.of("CANCEL_REQUESTED", "REFUND_PROCESSING", "CANCELED"),
        null,
        null,
        null);

    assertEquals(Set.of(OrderStatus.REFUND_REQUESTED, OrderStatus.REFUNDED), query.statuses());
  }
}
