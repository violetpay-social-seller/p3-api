package io.point3.p3api.inquiry.application.result;

import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import java.util.UUID;

public record SubmitPreOrderResult(UUID inquiryId, UUID contextProductId, UUID submissionId) {

  public static SubmitPreOrderResult from(Inquiry inquiry, OrderFormSubmission submission) {
    return new SubmitPreOrderResult(
        inquiry.getId(), inquiry.getContextProductId(), submission.getId());
  }
}
