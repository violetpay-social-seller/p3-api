package io.point3.p3api.payment.application.port;

import io.point3.p3api.payment.application.result.Point3CaptureResult;
import io.point3.p3api.payment.application.result.Point3PaymentSession;

public interface Point3PaymentPort {

  Point3PaymentSession createSession(long amount, String productName, String displayMerchantName);

  Point3CaptureResult capture(String sessionId);

  Point3RefundResult refund(String sessionId, long amount, String reason, String idempotencyKey);
}
