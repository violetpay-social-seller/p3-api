package io.point3.p3api.inquiry.application.submit;

import io.point3.p3api.inquiry.domain.entity.Inquiry;
import java.util.UUID;

public record SubmitPreOrderResult(UUID inquiryId, UUID contextProductId) {

  public static SubmitPreOrderResult from(Inquiry inquiry) {
    return new SubmitPreOrderResult(inquiry.getId(), inquiry.getContextProductId());
  }
}
