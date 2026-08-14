package io.point3.p3api.inquiry.controller.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record SubmitPreOrderRequest(
    UUID productId,
    @NotNull UUID clientRequestId,
    @NotNull UUID orderFormTemplateId,
    @Valid @NotNull List<FormAnswer> formAnswers,
    @Valid @NotNull List<ProductOptionSelection> productOptionSelections) {

  public record FormAnswer(@NotNull UUID fieldId, @NotNull JsonNode value) {}

  public record ProductOptionSelection(
      @NotNull UUID optionGroupId, @NotNull List<UUID> optionIds) {}
}
