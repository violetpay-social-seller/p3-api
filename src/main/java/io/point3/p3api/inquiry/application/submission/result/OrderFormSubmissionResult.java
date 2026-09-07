package io.point3.p3api.inquiry.application.submission.result;

import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import io.point3.p3api.order.application.result.OrderOptionRow;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record OrderFormSubmissionResult(
    UUID id,
    UUID inquiryId,
    UUID templateId,
    UUID submittedBy,
    LocalDate pickupDate,
    LocalTime pickupTime,
    String answers,
    List<OrderFormReferenceAssetResult> referenceAssets,
    List<OrderOptionRow> optionRows,
    boolean cancellationRefundAgreed,
    Instant submittedAt) {

  public OrderFormSubmissionResult {
    referenceAssets = referenceAssets == null ? List.of() : List.copyOf(referenceAssets);
    optionRows = List.copyOf(optionRows);
  }

  public static OrderFormSubmissionResult from(
      OrderFormSubmission submission,
      String answers,
      List<OrderFormReferenceAssetResult> referenceAssets,
      List<OrderOptionRow> optionRows) {
    return new OrderFormSubmissionResult(
        submission.getId(),
        submission.getInquiryId(),
        submission.getTemplateId(),
        submission.getSubmittedBy(),
        submission.getPickupDate(),
        submission.getPickupTime(),
        answers,
        referenceAssets,
        optionRows,
        submission.isCancellationRefundAgreed(),
        submission.getSubmittedAt());
  }

  @Override
  public List<OrderFormReferenceAssetResult> referenceAssets() {
    return List.copyOf(referenceAssets);
  }

  @Override
  public List<OrderOptionRow> optionRows() {
    return List.copyOf(optionRows);
  }
}
