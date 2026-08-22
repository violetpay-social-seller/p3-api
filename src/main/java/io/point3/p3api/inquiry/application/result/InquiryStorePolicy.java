package io.point3.p3api.inquiry.application.result;

import io.point3.p3api.store.domain.entity.Store;

public record InquiryStorePolicy(String orderNotice, String cancellationRefundPolicy) {
  public static InquiryStorePolicy from(Store store) {
    return new InquiryStorePolicy(store.getOrderNotice(), store.getCancellationRefundPolicy());
  }
}
