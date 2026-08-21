package io.point3.p3api.inquiry.controller.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record CreateOrderFormDraftRequest(
    @NotNull UUID orderFormTemplateId,
    @NotNull LocalDate pickupDate,
    @NotNull LocalTime pickupTime,
    boolean noticeAgreed,
    UUID selectedGalleryItemId,
    @Valid @NotNull List<FormAnswer> formAnswers,
    @Valid @NotNull List<ReferenceAsset> referenceAssets) {

  public record FormAnswer(@NotNull UUID fieldId, @NotNull JsonNode value) {}

  public record ReferenceAsset(
      @NotNull UUID assetId,
      @NotBlank String source,
      @Min(0) int sortOrder) {}
}
