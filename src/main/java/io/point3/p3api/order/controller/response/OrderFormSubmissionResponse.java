package io.point3.p3api.order.controller.response;

import io.point3.p3api.inquiry.application.submission.result.OrderFormSubmissionResult;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record OrderFormSubmissionResponse(
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

  public static OrderFormSubmissionResponse from(OrderFormSubmissionResult submission) {
    return new OrderFormSubmissionResponse(
        submission.id(),
        submission.inquiryId(),
        submission.templateId(),
        submission.submittedBy(),
        submission.pickupDate(),
        submission.pickupTime(),
        submission.answers(),
        submission.referenceAssets(),
        submission.cancellationRefundAgreed(),
        submission.submittedAt());
  }
}
