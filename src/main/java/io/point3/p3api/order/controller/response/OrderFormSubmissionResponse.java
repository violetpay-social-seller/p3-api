package io.point3.p3api.order.controller.response;

import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import java.time.Instant;
import java.util.UUID;

public record OrderFormSubmissionResponse(
    UUID id,
    UUID inquiryId,
    UUID templateId,
    UUID submittedBy,
    UUID productId,
    String productSnapshot,
    String productOptionSnapshot,
    String answers,
    Instant submittedAt) {

  public static OrderFormSubmissionResponse from(OrderFormSubmission submission) {
    return new OrderFormSubmissionResponse(
        submission.getId(),
        submission.getInquiryId(),
        submission.getTemplateId(),
        submission.getSubmittedBy(),
        submission.getProductId(),
        submission.getProductSnapshot(),
        submission.getProductOptionSnapshot(),
        submission.getAnswers(),
        submission.getSubmittedAt());
  }
}
