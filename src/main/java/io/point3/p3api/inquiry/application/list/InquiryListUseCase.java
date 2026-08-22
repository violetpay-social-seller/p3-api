package io.point3.p3api.inquiry.application.list;

import io.point3.p3api.inquiry.application.result.InquiryListItem;
import io.point3.p3api.inquiry.domain.type.InquiryStatus;

import java.util.List;
import java.util.UUID;

public interface InquiryListUseCase {
  List<InquiryListItem> getBuyerInquiries(UUID buyerUserId, InquiryStatus status);

  List<InquiryListItem> getSellerInquiries(UUID storeId, UUID sellerUserId, InquiryStatus status);


  void markBuyerRead(UUID inquiryId, UUID buyerUserId);

  void markSellerRead(UUID inquiryId, UUID storeId);
}
