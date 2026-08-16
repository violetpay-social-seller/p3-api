package io.point3.p3api.order.application.send;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SendOrderConfirmationCommand(
    UUID inquiryId,
    UUID storeId,
    UUID sellerUserId,
    UUID orderFormSubmissionId,
    String confirmationTitle,
    String summaryText,
    long amount,
    Instant pickupAt,
    List<AdditionalItem> additionalItems,
    String sellerNote) {

  public record AdditionalItem(String label, String value, Long amount) {}
}
