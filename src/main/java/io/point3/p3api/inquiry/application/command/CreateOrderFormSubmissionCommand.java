package io.point3.p3api.inquiry.application.command;

import com.fasterxml.jackson.databind.JsonNode;
import io.point3.p3api.inquiry.domain.type.OrderFormReferenceAssetSource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record CreateOrderFormSubmissionCommand(
    UUID storeId,
    UUID buyerUserId,
    UUID inquiryId,
    UUID orderFormTemplateId,
    List<FormAnswer> formAnswers,
    PickupRequest pickupRequest,
    NoticeAgreement noticeAgreement,
    List<ReferenceAsset> referenceAssets) {

  public CreateOrderFormSubmissionCommand {
    formAnswers = List.copyOf(formAnswers);
    referenceAssets = referenceAssets == null ? List.of() : List.copyOf(referenceAssets);
  }

  @Override
  public List<FormAnswer> formAnswers() {
    return List.copyOf(formAnswers);
  }

  @Override
  public List<ReferenceAsset> referenceAssets() {
    return List.copyOf(referenceAssets);
  }

  public record FormAnswer(UUID fieldId, JsonNode value) {}

  public record PickupRequest(LocalDate pickupDate, LocalTime pickupTime) {}

  public record NoticeAgreement(boolean agreed) {}

  public record ReferenceAsset(UUID assetId, OrderFormReferenceAssetSource source, int sortOrder) {}

  public static List<ReferenceAsset> emptyReferenceAssets() {
    return List.of();
  }
}
