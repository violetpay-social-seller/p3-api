package io.point3.p3api.payment.application.query;

import io.point3.p3api.payment.application.result.PaymentCtaResult;
import java.util.UUID;

public interface PaymentCtaQueryUseCase {
  PaymentCtaResult getBuyerConfirmationCta(UUID inquiryId, UUID confirmationId, UUID buyerUserId);
}
