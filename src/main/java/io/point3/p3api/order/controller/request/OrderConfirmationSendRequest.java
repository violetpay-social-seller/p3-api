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
    @NotNull UUID orderFormSubmissionId,
    @NotBlank @Size(max = 150) String confirmationTitle,
    @NotBlank String summaryText,
    @Min(0) long amount,
    @NotNull Instant pickupAt,
    @Valid @NotNull List<AdditionalItem> additionalItems,
    String sellerNote) {

  public OrderConfirmationSendRequest {
    additionalItems = additionalItems == null ? null : List.copyOf(additionalItems);
  }

  @Override
  public List<AdditionalItem> additionalItems() {
    return additionalItems == null ? null : List.copyOf(additionalItems);
  }

  public record AdditionalItem(
      @NotBlank @Size(max = 100) String label, @NotBlank String value, Long amount) {}
}
