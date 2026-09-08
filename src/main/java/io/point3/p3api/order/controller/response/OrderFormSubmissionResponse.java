package io.point3.p3api.order.controller.response;

import io.point3.p3api.inquiry.application.submission.result.OrderFormReferenceAssetResult;
import io.point3.p3api.inquiry.application.submission.result.OrderFormSubmissionResult;
import io.point3.p3api.inquiry.domain.type.OrderFormReferenceAssetSource;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record OrderFormSubmissionResponse(
    UUID id,
    UUID inquiryId,
    UUID templateId,
    UUID submittedBy,
    LocalDate pickupDate,
    LocalTime pickupTime,
    String answers,
    List<ReferenceAssetResponse> referenceAssets,
    List<OrderOptionRowResponse> optionRows,
    boolean cancellationRefundAgreed,
    Instant submittedAt) {

  public OrderFormSubmissionResponse {
    referenceAssets = List.copyOf(referenceAssets);
    optionRows = List.copyOf(optionRows);
  }

  public static OrderFormSubmissionResponse from(OrderFormSubmissionResult submission) {
    return new OrderFormSubmissionResponse(
        submission.id(),
        submission.inquiryId(),
        submission.templateId(),
        submission.submittedBy(),
        submission.pickupDate(),
        submission.pickupTime(),
        submission.answers(),
        submission.referenceAssets().stream().map(ReferenceAssetResponse::from).toList(),
        submission.optionRows().stream().map(OrderOptionRowResponse::from).toList(),
        submission.cancellationRefundAgreed(),
        submission.submittedAt());
  }

  @Override
  public List<ReferenceAssetResponse> referenceAssets() {
    return List.copyOf(referenceAssets);
  }

  @Override
  public List<OrderOptionRowResponse> optionRows() {
    return List.copyOf(optionRows);
  }

  public record ReferenceAssetResponse(
      UUID assetId,
      OrderFormReferenceAssetSource source,
      int sortOrder,
      String status,
      String deliveryUrl,
      List<VariantResponse> variants) {

    public ReferenceAssetResponse {
      variants = List.copyOf(variants);
    }

    private static ReferenceAssetResponse from(OrderFormReferenceAssetResult result) {
      return new ReferenceAssetResponse(
          result.assetId(),
          result.source(),
          result.sortOrder(),
          result.status(),
          result.deliveryUrl(),
          result.variants().stream().map(VariantResponse::from).toList());
    }

    @Override
    public List<VariantResponse> variants() {
      return List.copyOf(variants);
    }
  }

  public record VariantResponse(String type, String deliveryUrl, int width, int height) {
    private static VariantResponse from(OrderFormReferenceAssetResult.Variant variant) {
      return new VariantResponse(
          variant.type(), variant.deliveryUrl(), variant.width(), variant.height());
    }
  }
}
