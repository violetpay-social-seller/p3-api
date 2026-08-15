package io.point3.p3api.inquiry.controller.response;

import io.point3.p3api.inquiry.application.result.SubmitPreOrderResult;
import java.util.UUID;

public record SubmitPreOrderResponse(UUID inquiryId, UUID contextProductId) {

  public static SubmitPreOrderResponse from(SubmitPreOrderResult result) {
    return new SubmitPreOrderResponse(result.inquiryId(), result.contextProductId());
  }
}
