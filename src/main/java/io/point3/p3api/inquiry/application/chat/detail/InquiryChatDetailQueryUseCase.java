package io.point3.p3api.inquiry.application.chat.detail;

import io.point3.p3api.inquiry.domain.entity.Inquiry;

public interface InquiryChatDetailQueryUseCase {

  InquiryChatDetail getBuyerDetail(Inquiry inquiry);

  InquiryChatDetail getSellerDetail(Inquiry inquiry);
}
