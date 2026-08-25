package io.point3.p3api.payment.application.prepare;

import io.point3.p3api.payment.application.result.PaymentPreparationResult;

public interface PaymentPrepareUseCase {

  PaymentPreparationResult prepare(PreparePaymentCommand command);
}
