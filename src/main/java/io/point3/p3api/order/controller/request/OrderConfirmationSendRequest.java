package io.point3.p3api.order.controller.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderConfirmationSendRequest(
    UUID orderFormSubmissionId,
    @NotBlank @Size(max = 150) String confirmationTitle,
    @NotBlank String summaryText,
    @Min(1) long amount,
    @NotNull Instant pickupAt,
    @Valid List<ConfirmedOptionPrice> confirmedOptionPrices,
    @Valid @NotNull List<AdditionalItem> additionalItems,
    String sellerNote) {

  public OrderConfirmationSendRequest {
    confirmedOptionPrices =
        confirmedOptionPrices == null ? List.of() : List.copyOf(confirmedOptionPrices);
    additionalItems = additionalItems == null ? null : List.copyOf(additionalItems);
  }

  @Override
  public List<ConfirmedOptionPrice> confirmedOptionPrices() {
    return List.copyOf(confirmedOptionPrices);
  }

  @Override
  public List<AdditionalItem> additionalItems() {
    return additionalItems == null ? null : List.copyOf(additionalItems);
  }

  public record AdditionalItem(
      @NotBlank @Size(max = 100) String label,
      @NotBlank String value,
      @NotNull @Min(0) Long amount) {}

  public record ConfirmedOptionPrice(
      @NotNull UUID optionGroupId,
      @NotBlank String optionValue,
      @NotNull @Min(0) Long amount) {}
}
