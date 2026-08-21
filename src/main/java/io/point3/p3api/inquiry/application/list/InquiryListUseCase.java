package io.point3.p3api.inquiry.application.list;

import io.point3.p3api.inquiry.application.result.InquiryListItem;
import java.util.List;
import java.util.UUID;

public interface InquiryListUseCase {
  List<InquiryListItem> getBuyerInquiries(UUID buyerUserId);

  List<InquiryListItem> getSellerInquiries(UUID storeId, UUID sellerUserId);

  void markBuyerRead(UUID inquiryId, UUID buyerUserId);

  void markSellerRead(UUID inquiryId, UUID storeId);
}
