package io.point3.p3api.inquiry.controller.response;

import io.point3.p3api.inquiry.application.result.OrderFormDraftResult;
import java.time.Instant;

public record OrderFormDraftResponse(String draftKey, Instant expiresAt) {

  public static OrderFormDraftResponse from(OrderFormDraftResult result) {
    return new OrderFormDraftResponse(result.draftKey(), result.expiresAt());
  }
}
