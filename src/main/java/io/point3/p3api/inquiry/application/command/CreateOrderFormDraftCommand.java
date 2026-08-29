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
    boolean cancellationRefundAgreed,
    List<FormAnswer> formAnswers,
    List<ReferenceAsset> startReferenceAssets) {

  public CreateOrderFormDraftCommand(
      UUID storeId,
      UUID orderFormTemplateId,
      LocalDate pickupDate,
      LocalTime pickupTime,
      boolean noticeAgreed,
      List<FormAnswer> formAnswers,
      List<ReferenceAsset> startReferenceAssets) {
    this(
        storeId,
        orderFormTemplateId,
        pickupDate,
        pickupTime,
        noticeAgreed,
        false,
        formAnswers,
        startReferenceAssets);
  }

  public CreateOrderFormDraftCommand {
    formAnswers = List.copyOf(formAnswers);
    startReferenceAssets =
        startReferenceAssets == null ? List.of() : List.copyOf(startReferenceAssets);
  }

  @Override
  public List<FormAnswer> formAnswers() {
    return List.copyOf(formAnswers);
  }

  @Override
  public List<ReferenceAsset> startReferenceAssets() {
    return List.copyOf(startReferenceAssets);
  }

  public record FormAnswer(UUID fieldId, JsonNode value) {}

  public record ReferenceAsset(
      @NotNull UUID assetId,
      @NotNull OrderFormReferenceAssetSource source,
      @Min(0) int sortOrder) {}
}
