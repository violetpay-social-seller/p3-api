package io.point3.p3api.inquiry.controller.response;

import io.point3.p3api.inquiry.domain.entity.Inquiry;
import java.util.UUID;

public record InquiryOpenResponse(UUID inquiryId) {

  public static InquiryOpenResponse from(Inquiry inquiry) {
    return new InquiryOpenResponse(inquiry.getId());
  }
}
