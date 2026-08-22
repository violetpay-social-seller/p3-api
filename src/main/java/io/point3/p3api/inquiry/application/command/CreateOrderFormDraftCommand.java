package io.point3.p3api.inquiry.application.command;

import com.fasterxml.jackson.databind.JsonNode;
import io.point3.p3api.inquiry.domain.type.OrderFormReferenceAssetSource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record CreateOrderFormDraftCommand(
    UUID storeId,
    UUID orderFormTemplateId,
    LocalDate pickupDate,
    LocalTime pickupTime,
    boolean noticeAgreed,
    List<FormAnswer> formAnswers,
    List<ReferenceAsset> referenceAssets) {
  public record FormAnswer(UUID fieldId, JsonNode value) {}

  public record ReferenceAsset(
      @NotNull UUID assetId,
      @NotNull OrderFormReferenceAssetSource source,
      @Min(0) int sortOrder) {}
}
