package io.point3.p3api.inquiry.application.submission.result;

import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record OrderFormSubmissionResult(
    UUID id,
    UUID inquiryId,
    UUID templateId,
    UUID submittedBy,
    LocalDate pickupDate,
    LocalTime pickupTime,
    String answers,
    String referenceAssets,
    boolean cancellationRefundAgreed,
    Instant submittedAt) {

  public static OrderFormSubmissionResult from(OrderFormSubmission submission, String answers) {
    return new OrderFormSubmissionResult(
        submission.getId(),
        submission.getInquiryId(),
        submission.getTemplateId(),
        submission.getSubmittedBy(),
        submission.getPickupDate(),
        submission.getPickupTime(),
        answers,
        submission.getReferenceAssets(),
        submission.isCancellationRefundAgreed(),
        submission.getSubmittedAt());
  }
}
