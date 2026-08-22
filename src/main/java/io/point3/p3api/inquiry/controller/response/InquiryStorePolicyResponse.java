package io.point3.p3api.inquiry.controller.response;

import io.point3.p3api.inquiry.application.result.InquiryStorePolicy;

public record InquiryStorePolicyResponse(String orderNotice, String cancellationRefundPolicy) {
  public static InquiryStorePolicyResponse from(InquiryStorePolicy policy) {
    return new InquiryStorePolicyResponse(policy.orderNotice(), policy.cancellationRefundPolicy());
  }
}
