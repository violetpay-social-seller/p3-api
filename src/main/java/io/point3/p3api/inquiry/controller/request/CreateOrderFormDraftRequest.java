package io.point3.p3api.inquiry.controller.request;

import io.point3.p3api.inquiry.domain.type.OrderFormReferenceAssetSource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class CreateOrderFormDraftRequest {

  @NotNull
  private UUID orderFormTemplateId;

  @NotNull
  private LocalDate pickupDate;

  @NotNull
  private LocalTime pickupTime;

  private boolean noticeAgreed;
  private boolean cancellationRefundAgreed;

  @Valid
  @NotNull
  private List<FormAnswer> formAnswers;

  @Valid
  private ReferenceAsset startReferenceAsset;

  private boolean startReferenceAssetProvided;

  public UUID orderFormTemplateId() {
    return orderFormTemplateId;
  }

  public LocalDate pickupDate() {
    return pickupDate;
  }

  public LocalTime pickupTime() {
    return pickupTime;
  }

  public boolean noticeAgreed() {
    return noticeAgreed;
  }

  public boolean cancellationRefundAgreed() {
    return cancellationRefundAgreed;
  }

  public List<FormAnswer> formAnswers() {
    return formAnswers == null ? null : List.copyOf(formAnswers);
  }

  public ReferenceAsset startReferenceAsset() {
    return startReferenceAsset;
  }

  public boolean startReferenceAssetProvided() {
    return startReferenceAssetProvided;
  }

  public void setOrderFormTemplateId(UUID orderFormTemplateId) {
    this.orderFormTemplateId = orderFormTemplateId;
  }

  public void setPickupDate(LocalDate pickupDate) {
    this.pickupDate = pickupDate;
  }

  public void setPickupTime(LocalTime pickupTime) {
    this.pickupTime = pickupTime;
  }

  public void setNoticeAgreed(boolean noticeAgreed) {
    this.noticeAgreed = noticeAgreed;
  }

  public void setCancellationRefundAgreed(boolean cancellationRefundAgreed) {
    this.cancellationRefundAgreed = cancellationRefundAgreed;
  }

  public void setFormAnswers(List<FormAnswer> formAnswers) {
    this.formAnswers = formAnswers == null ? null : List.copyOf(formAnswers);
  }

  public void setStartReferenceAsset(ReferenceAsset startReferenceAsset) {
    this.startReferenceAsset = startReferenceAsset;
    this.startReferenceAssetProvided = true;
  }

  public void setStartReferenceAssets(Object ignored) {
    // 구 startReferenceAssets 필드가 함께 와도 draft 생성 계약에는 반영하지 않음
  }

  public record FormAnswer(
      @NotNull UUID optionGroupId, @NotNull Object value) {}

  public record ReferenceAsset(
      @NotNull UUID assetId, @NotNull OrderFormReferenceAssetSource source) {}
}
