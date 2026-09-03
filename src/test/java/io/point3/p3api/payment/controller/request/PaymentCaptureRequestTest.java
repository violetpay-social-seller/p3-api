package io.point3.p3api.payment.controller.request;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class PaymentCaptureRequestTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void mapsSdkOrderIdToSessionId() throws Exception {
    PaymentCaptureRequest request = objectMapper.readValue("""
            {
              "orderId": "pymt_sess-123",
              "payerId": "payer-123"
            }
            """, PaymentCaptureRequest.class);

    assertEquals("pymt_sess-123", request.sessionId());
    assertEquals("payer-123", request.payerId());
  }
}
