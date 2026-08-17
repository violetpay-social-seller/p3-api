package io.point3.p3api.inquiry.controller.response;

import io.point3.p3api.inquiry.application.result.OrderFormDraftConsumeResult;
import java.util.UUID;

public record OrderFormDraftConsumeResponse(UUID inquiryId, UUID submissionId) {

  public static OrderFormDraftConsumeResponse from(OrderFormDraftConsumeResult result) {
    return new OrderFormDraftConsumeResponse(
        result.inquiry().getId(), result.submission().getId());
  }
}
