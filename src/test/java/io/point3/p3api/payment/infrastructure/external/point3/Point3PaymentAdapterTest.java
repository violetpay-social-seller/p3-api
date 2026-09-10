package io.point3.p3api.payment.infrastructure.external.point3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.payment.application.result.Point3CaptureResult;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class Point3PaymentAdapterTest {

  @ParameterizedTest
  @CsvSource({
    "captured, CAPTURED",
    "failed, FAILED",
    "expired, FAILED",
    "processing, PROCESSING",
    "created, PROCESSING"
  })
  void mapsPoint3SessionStatus(String point3Status, Point3CaptureResult.Status expectedStatus) {
    assertEquals(expectedStatus, Point3PaymentAdapter.toCaptureStatus(point3Status));
  }

  @Test
  void readsCreateSessionResponseWithExtraFields() throws Exception {
    Class<?> responseType = Class.forName(
        "io.point3.p3api.payment.infrastructure.external.point3.Point3PaymentAdapter$CreatePaymentSessionResponse");
    Object response = new ObjectMapper()
        .readValue(
            """
            {
              "object": "paymentSession",
              "id": "pymt_sess-test",
              "status": "created",
              "clientId": "client-test",
              "amount": 10000,
              "currency": "KRW",
              "supplyAmount": 9091,
              "taxFreeAmount": 0,
              "vat": 909,
              "productName": "cake",
              "tradeOpt": "GENERAL",
              "createdAt": "2026-08-21T00:00:00.000Z",
              "updatedAt": "2026-08-21T00:00:00.000Z"
            }
            """,
            responseType);

    Method id = responseType.getDeclaredMethod("id");
    Method amount = responseType.getDeclaredMethod("amount");
    id.setAccessible(true);
    amount.setAccessible(true);

    assertEquals("pymt_sess-test", id.invoke(response));
    assertEquals(10000L, amount.invoke(response));
  }
}
