package io.point3.p3api.inquiry.application.list;

import io.point3.p3api.chat.application.port.ChatTimelineItemPort;
import io.point3.p3api.inquiry.application.chat.InquiryChatAccessService;
import io.point3.p3api.inquiry.application.chat.InquiryChatDetailQueryUseCase;
import io.point3.p3api.inquiry.application.port.InquiryPersistencePort;
import io.point3.p3api.inquiry.application.result.InquiryChatDetail;
import io.point3.p3api.inquiry.application.result.InquiryListItem;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class InquiryListService implements InquiryListUseCase {
  private final InquiryPersistencePort inquiryPersistencePort;
  private final InquiryChatAccessService inquiryChatAccessService;
  private final InquiryChatDetailQueryUseCase inquiryChatDetailQueryUseCase;
  private final ChatTimelineItemPort chatTimelineItemPort;

  @Override
  @Transactional(readOnly = true)
  public List<InquiryListItem> getBuyerInquiries(UUID buyerUserId) {
    return inquiryPersistencePort.findAllByBuyerUserId(buyerUserId).stream()
        .map(inquiry -> toBuyerItem(inquiry, buyerUserId))
        .sorted(byLatestEvent())
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<InquiryListItem> getSellerInquiries(UUID storeId, UUID sellerUserId) {
    return inquiryPersistencePort.findAllByStoreId(storeId).stream()
        .map(inquiry -> toSellerItem(inquiry, sellerUserId))
        .sorted(byLatestEvent())
        .toList();
  }

  @Override
  public void markBuyerRead(UUID inquiryId, UUID buyerUserId) {
    inquiryChatAccessService.getBuyerInquiry(inquiryId, buyerUserId).markBuyerRead(Instant.now());
  }

  @Override
  public void markSellerRead(UUID inquiryId, UUID storeId) {
    inquiryChatAccessService.getSellerInquiry(inquiryId, storeId).markSellerRead(Instant.now());
  }

  private InquiryListItem toBuyerItem(Inquiry inquiry, UUID buyerUserId) {
    InquiryChatDetail detail = inquiryChatDetailQueryUseCase.getBuyerDetail(inquiry);
    return toItem(inquiry, buyerUserId, inquiry.getBuyerLastReadAt(), detail);
  }

  private InquiryListItem toSellerItem(Inquiry inquiry, UUID sellerUserId) {
    InquiryChatDetail detail = inquiryChatDetailQueryUseCase.getSellerDetail(inquiry);
    return toItem(inquiry, sellerUserId, inquiry.getSellerLastReadAt(), detail);
  }

  private InquiryListItem toItem(
      Inquiry inquiry, UUID readerUserId, Instant readAt, InquiryChatDetail detail) {
    Instant latestEventAt = chatTimelineItemPort.findLatestCreatedAt(inquiry.getId());
    long unreadCount = chatTimelineItemPort.countUnread(inquiry.getId(), readerUserId, readAt);
    return new InquiryListItem(
        detail, unreadCount, latestEventAt == null ? inquiry.getCreatedAt() : latestEventAt);
  }

  private Comparator<InquiryListItem> byLatestEvent() {
    return Comparator.comparing(InquiryListItem::latestEventAt).reversed();
  }
}
