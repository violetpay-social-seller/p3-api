package io.point3.p3api.inquiry.controller.request;

import io.point3.p3api.inquiry.domain.type.OrderFormReferenceAssetSource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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
    @Valid @NotNull List<FormAnswer> formAnswers,
    @Valid @NotNull List<ReferenceAsset> referenceAssets) {

  public CreateOrderFormDraftRequest {
    formAnswers = formAnswers == null ? null : List.copyOf(formAnswers);
    referenceAssets = referenceAssets == null ? null : List.copyOf(referenceAssets);
  }

  @Override
  public List<FormAnswer> formAnswers() {
    return formAnswers == null ? null : List.copyOf(formAnswers);
  }

  @Override
  public List<ReferenceAsset> referenceAssets() {
    return referenceAssets == null ? null : List.copyOf(referenceAssets);
  }

  public record FormAnswer(@NotNull UUID fieldId, @NotNull Object value) {}

  public record ReferenceAsset(
      @NotNull UUID assetId,
      @NotNull OrderFormReferenceAssetSource source,
      @Min(0) int sortOrder) {}
}
