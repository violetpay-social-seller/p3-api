package io.point3.p3api.order.application.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RequestOrderRefundCommandTest {

  @Test
  @DisplayName("구매자 환불 요청 사유가 없으면 기본 사유로 처리한다")
  void usesDefaultReasonWhenReasonIsBlank() {
    RequestOrderRefundCommand nullReason =
        RequestOrderRefundCommand.of(UUID.randomUUID(), UUID.randomUUID(), null);
    RequestOrderRefundCommand blankReason =
        RequestOrderRefundCommand.of(UUID.randomUUID(), UUID.randomUUID(), " ");

    assertEquals("구매자 취소 요청", nullReason.reason());
    assertEquals("구매자 취소 요청", blankReason.reason());
  }
}
