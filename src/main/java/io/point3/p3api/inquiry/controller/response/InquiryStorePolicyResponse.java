package io.point3.p3api.inquiry.controller.response;

public record InquiryStorePolicyResponse(String orderNotice, String cancellationRefundPolicy) {
  public static InquiryStorePolicyResponse empty() {
    return new InquiryStorePolicyResponse(null, null);
  }
}
