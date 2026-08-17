package io.point3.p3api.inquiry.application.result;

import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;

public record OrderFormDraftConsumeResult(Inquiry inquiry, OrderFormSubmission submission) {

  public static OrderFormDraftConsumeResult from(Inquiry inquiry, OrderFormSubmission submission) {
    return new OrderFormDraftConsumeResult(inquiry, submission);
  }
}
