package io.point3.p3api.order.controller.response;

import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import java.time.Instant;
import java.util.UUID;

public record OrderFormSubmissionResponse(
    UUID id,
    UUID inquiryId,
    UUID templateId,
    UUID submittedBy,
    String answers,
    String referenceAssets,
    Instant submittedAt) {

  public static OrderFormSubmissionResponse from(OrderFormSubmission submission) {
    return new OrderFormSubmissionResponse(
        submission.getId(),
        submission.getInquiryId(),
        submission.getTemplateId(),
        submission.getSubmittedBy(),
        submission.getAnswers(),
        submission.getReferenceAssets(),
        submission.getSubmittedAt());
  }
}
