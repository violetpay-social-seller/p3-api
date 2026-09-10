package io.point3.p3api.inquiry.application.command;

import java.util.UUID;

public record RequestOrderFormRevisionCommand(
    UUID inquiryId, UUID submissionId, UUID storeId, UUID sellerUserId) {

  public static RequestOrderFormRevisionCommand of(
      UUID inquiryId, UUID submissionId, UUID storeId, UUID sellerUserId) {
    return new RequestOrderFormRevisionCommand(inquiryId, submissionId, storeId, sellerUserId);
  }
}
