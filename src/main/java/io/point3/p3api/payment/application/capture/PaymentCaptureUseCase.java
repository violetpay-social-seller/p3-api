package io.point3.p3api.payment.application.capture;

import io.point3.p3api.payment.application.result.PaymentCaptureResult;

public interface PaymentCaptureUseCase {

  PaymentCaptureResult capture(CapturePaymentCommand command);
}
