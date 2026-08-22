package io.point3.p3api.inquiry.application.chat;

import io.point3.p3api.inquiry.application.result.InquiryStorePolicy;
import io.point3.p3api.inquiry.domain.entity.Inquiry;

public interface InquiryStorePolicyQueryUseCase {
  InquiryStorePolicy get(Inquiry inquiry);
}
