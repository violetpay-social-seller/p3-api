package io.point3.p3api.payment.application.prepare;

import java.util.UUID;

public record PreparePaymentCommand(UUID inquiryId, UUID confirmationId, UUID buyerUserId) {

  public static PreparePaymentCommand of(UUID inquiryId, UUID confirmationId, UUID buyerUserId) {
    return new PreparePaymentCommand(inquiryId, confirmationId, buyerUserId);
  }
}
