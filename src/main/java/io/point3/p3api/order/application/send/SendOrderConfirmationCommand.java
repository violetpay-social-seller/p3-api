package io.point3.p3api.order.application.send;

import io.point3.p3api.order.application.price.ConfirmedOptionPrice;
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
    List<ConfirmedOptionPrice> confirmedOptionPrices,
    List<AdditionalItem> additionalItems,
    String sellerNote) {

  public SendOrderConfirmationCommand {
    confirmedOptionPrices =
        confirmedOptionPrices == null ? List.of() : List.copyOf(confirmedOptionPrices);
    additionalItems = additionalItems == null ? List.of() : List.copyOf(additionalItems);
  }

  public SendOrderConfirmationCommand(
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
    this(
        inquiryId,
        storeId,
        sellerUserId,
        orderFormSubmissionId,
        confirmationTitle,
        summaryText,
        amount,
        pickupAt,
        List.of(),
        additionalItems,
        sellerNote);
  }

  @Override
  public List<ConfirmedOptionPrice> confirmedOptionPrices() {
    return List.copyOf(confirmedOptionPrices);
  }

  @Override
  public List<AdditionalItem> additionalItems() {
    return List.copyOf(additionalItems);
  }

  public record AdditionalItem(String label, String value, Long amount) {}
}
