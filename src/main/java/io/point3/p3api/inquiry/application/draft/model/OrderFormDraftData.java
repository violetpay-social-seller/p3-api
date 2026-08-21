package io.point3.p3api.inquiry.application.draft.model;

import com.fasterxml.jackson.databind.JsonNode;
import io.point3.p3api.inquiry.application.command.CreateOrderFormDraftCommand;
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
    UUID selectedGalleryItemId,
    List<FormAnswer> formAnswers,
    List<ReferenceAsset> referenceAssets) {

  public record FormAnswer(UUID fieldId, JsonNode value) {}

  public record ReferenceAsset(UUID assetId, String source, int sortOrder) {}

  public static OrderFormDraftData from(CreateOrderFormDraftCommand command) {
    return new OrderFormDraftData(
        command.storeId(),
        command.orderFormTemplateId(),
        command.pickupDate(),
        command.pickupTime(),
        command.noticeAgreed(),
        command.selectedGalleryItemId(),
        command.formAnswers().stream()
            .map(answer -> new FormAnswer(answer.fieldId(), answer.value()))
            .toList(),
        command.referenceAssets().stream()
            .map(asset -> new ReferenceAsset(asset.assetId(), asset.source(), asset.sortOrder()))
            .toList());
  }
}
