package io.point3.p3api.inquiry.application.draft.model;

import com.fasterxml.jackson.databind.JsonNode;
import io.point3.p3api.inquiry.application.command.CreateOrderFormDraftCommand;
import io.point3.p3api.inquiry.domain.type.OrderFormReferenceAssetSource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Redis에 보관되는 임시 주문서 데이터 모델
 */
public record OrderFormDraftData(
    UUID storeId,
    UUID orderFormTemplateId,
    LocalDate pickupDate,
    LocalTime pickupTime,
    boolean noticeAgreed,
    boolean cancellationRefundAgreed,
    List<FormAnswer> formAnswers,
    List<ReferenceAsset> startReferenceAssets) {

  public OrderFormDraftData(
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

  public OrderFormDraftData {
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

  public record ReferenceAsset(UUID assetId, OrderFormReferenceAssetSource source, int sortOrder) {}

  public static OrderFormDraftData from(CreateOrderFormDraftCommand command) {
    return new OrderFormDraftData(
        command.storeId(),
        command.orderFormTemplateId(),
        command.pickupDate(),
        command.pickupTime(),
        command.noticeAgreed(),
        command.cancellationRefundAgreed(),
        command.formAnswers().stream()
            .map(answer -> new FormAnswer(answer.fieldId(), answer.value()))
            .toList(),
        command.startReferenceAssets().stream()
            .map(asset -> new ReferenceAsset(asset.assetId(), asset.source(), asset.sortOrder()))
            .toList());
  }
}
