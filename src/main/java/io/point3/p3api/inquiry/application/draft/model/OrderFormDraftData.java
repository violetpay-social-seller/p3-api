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
    ReferenceAsset startReferenceAsset,
    boolean startReferenceAssetProvided) {

  public OrderFormDraftData(
      UUID storeId,
      UUID orderFormTemplateId,
      LocalDate pickupDate,
      LocalTime pickupTime,
      boolean noticeAgreed,
      List<FormAnswer> formAnswers,
      ReferenceAsset startReferenceAsset,
      boolean startReferenceAssetProvided) {
    this(
        storeId,
        orderFormTemplateId,
        pickupDate,
        pickupTime,
        noticeAgreed,
        false,
        formAnswers,
        startReferenceAsset,
        startReferenceAssetProvided);
  }

  public OrderFormDraftData {
    formAnswers = List.copyOf(formAnswers);
  }

  @Override
  public List<FormAnswer> formAnswers() {
    return List.copyOf(formAnswers);
  }

  public record FormAnswer(UUID optionGroupId, JsonNode value) {}

  public record ReferenceAsset(UUID assetId, OrderFormReferenceAssetSource source) {}

  public static OrderFormDraftData from(CreateOrderFormDraftCommand command) {
    return new OrderFormDraftData(
        command.storeId(),
        command.orderFormTemplateId(),
        command.pickupDate(),
        command.pickupTime(),
        command.noticeAgreed(),
        command.cancellationRefundAgreed(),
        command.formAnswers().stream()
            .map(answer -> new FormAnswer(answer.optionGroupId(), answer.value()))
            .toList(),
        command.startReferenceAsset() == null
            ? null
            : new ReferenceAsset(
                command.startReferenceAsset().assetId(),
                command.startReferenceAsset().source()),
        command.startReferenceAssetProvided());
  }
}
