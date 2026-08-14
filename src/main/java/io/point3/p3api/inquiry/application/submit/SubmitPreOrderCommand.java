package io.point3.p3api.inquiry.application.submit;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.UUID;

public record SubmitPreOrderCommand(
        UUID storeId,
        UUID buyerUserId,
        UUID productId,
        UUID clientRequestId,
        UUID orderFormTemplateId,
        List<FormAnswer> formAnswers,
        List<ProductOptionSelection> productOptionSelections) {

  public static SubmitPreOrderCommand of(
          UUID storeId,
          UUID buyerUserId,
          UUID productId,
          UUID clientRequestId,
          UUID orderFormTemplateId,
          List<FormAnswer> formAnswers,
          List<ProductOptionSelection> productOptionSelections) {
    return new SubmitPreOrderCommand(
            storeId,
            buyerUserId,
            productId,
            clientRequestId,
            orderFormTemplateId,
            formAnswers,
            productOptionSelections);
  }

  public record FormAnswer(UUID fieldId, JsonNode value) {}

  public record ProductOptionSelection(UUID optionGroupId, List<UUID> optionIds) {}
}
