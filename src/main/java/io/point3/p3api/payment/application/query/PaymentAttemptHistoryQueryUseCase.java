package io.point3.p3api.payment.application.query;

import io.point3.p3api.payment.application.result.PaymentAttemptResult;
import java.util.List;
import java.util.UUID;

public interface PaymentAttemptHistoryQueryUseCase {
  List<PaymentAttemptResult> getBuyerPaymentAttempts(
      UUID inquiryId, UUID confirmationId, UUID buyerUserId);
}
