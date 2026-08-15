package io.point3.p3api.inquiry.application.chat;

import io.point3.p3api.inquiry.application.result.InquiryChatDetail;
import io.point3.p3api.inquiry.domain.entity.Inquiry;

public interface InquiryChatDetailQueryUseCase {

  InquiryChatDetail getBuyerDetail(Inquiry inquiry);

  InquiryChatDetail getSellerDetail(Inquiry inquiry);
}
