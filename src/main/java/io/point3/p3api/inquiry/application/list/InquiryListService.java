package io.point3.p3api.inquiry.application.list;

import io.point3.p3api.chat.application.port.ChatTimelineItemPort;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.ChatErrorCode;
import io.point3.p3api.inquiry.application.chat.InquiryChatAccessService;
import io.point3.p3api.inquiry.application.chat.InquiryChatDetailQueryUseCase;
import io.point3.p3api.inquiry.application.port.InquiryPersistencePort;
import io.point3.p3api.inquiry.application.result.InquiryChatDetail;
import io.point3.p3api.inquiry.application.result.InquiryListItem;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.inquiry.domain.type.InquiryStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
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
  private final Clock clock;

  @Override
  @Transactional(readOnly = true)
  public List<InquiryListItem> getBuyerInquiries(UUID buyerUserId, InquiryStatus status) {
    return getBuyerInquiries(buyerUserId, status, false);
  }

  @Override
  @Transactional(readOnly = true)
  public List<InquiryListItem> getBuyerInquiries(
      UUID buyerUserId, InquiryStatus status, boolean unreadOnly) {
    return inquiryPersistencePort.findAllByBuyerUserId(buyerUserId).stream()
        .filter(inquiry -> isBuyerListTarget(inquiry, status))
        .map(inquiry -> toBuyerItem(inquiry, buyerUserId))
        .filter(item -> !unreadOnly || item.unreadCount() > 0)
        .sorted(byLatestEvent())
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<InquiryListItem> getSellerInquiries(
      UUID storeId, UUID sellerUserId, InquiryStatus status) {
    return getSellerInquiries(storeId, sellerUserId, status, false);
  }

  @Override
  @Transactional(readOnly = true)
  public List<InquiryListItem> getSellerInquiries(
      UUID storeId, UUID sellerUserId, InquiryStatus status, boolean unreadOnly) {
    return inquiryPersistencePort.findAllByStoreId(storeId).stream()
        .filter(inquiry -> isSellerListTarget(inquiry, status))
        .map(inquiry -> toSellerItem(inquiry, sellerUserId))
        .filter(item -> !unreadOnly || item.unreadCount() > 0)
        .sorted(byLatestEvent())
        .toList();
  }

  @Override
  public void markBuyerRead(UUID inquiryId, UUID buyerUserId) {
    inquiryChatAccessService
        .getBuyerInquiry(inquiryId, buyerUserId)
        .markBuyerRead(Instant.now(clock));
  }

  @Override
  public void markSellerRead(UUID inquiryId, UUID storeId) {
    inquiryChatAccessService
        .getSellerInquiry(inquiryId, storeId)
        .markSellerRead(Instant.now(clock));
  }

  @Override
  public void moveBuyerToTrash(UUID inquiryId, UUID buyerUserId) {
    Inquiry inquiry = inquiryChatAccessService.getBuyerInquiry(inquiryId, buyerUserId);
    ensureBuyerVisible(inquiry);
    inquiry.moveBuyerToTrash(Instant.now(clock));
  }

  @Override
  public void moveSellerToTrash(UUID inquiryId, UUID storeId) {
    Inquiry inquiry = inquiryChatAccessService.getSellerInquiry(inquiryId, storeId);
    ensureSellerVisible(inquiry);
    inquiry.moveSellerToTrash(Instant.now(clock));
  }

  @Override
  public void restoreBuyerFromTrash(UUID inquiryId, UUID buyerUserId) {
    Inquiry inquiry = inquiryChatAccessService.getBuyerInquiry(inquiryId, buyerUserId);
    ensureBuyerVisible(inquiry);
    inquiry.restoreBuyerFromTrash();
  }

  @Override
  public void restoreSellerFromTrash(UUID inquiryId, UUID storeId) {
    Inquiry inquiry = inquiryChatAccessService.getSellerInquiry(inquiryId, storeId);
    ensureSellerVisible(inquiry);
    inquiry.restoreSellerFromTrash();
  }

  @Override
  @Scheduled(cron = "0 0 3 * * *")
  public void purgeExpiredTrash() {
    Instant now = Instant.now(clock);
    Instant cutoff = ZonedDateTime.now(clock).minusMonths(1).toInstant();
    inquiryPersistencePort.purgeExpiredTrash(cutoff, now);
  }

  private InquiryListItem toBuyerItem(Inquiry inquiry, UUID buyerUserId) {
    InquiryChatDetail detail = inquiryChatDetailQueryUseCase.getBuyerDetail(inquiry);
    return toItem(
        inquiry, buyerUserId, inquiry.getBuyerLastReadAt(), detail, inquiry.statusForBuyer());
  }

  private InquiryListItem toSellerItem(Inquiry inquiry, UUID sellerUserId) {
    InquiryChatDetail detail = inquiryChatDetailQueryUseCase.getSellerDetail(inquiry);
    return toItem(
        inquiry, sellerUserId, inquiry.getSellerLastReadAt(), detail, inquiry.statusForSeller());
  }

  private InquiryListItem toItem(
      Inquiry inquiry,
      UUID readerUserId,
      Instant readAt,
      InquiryChatDetail detail,
      InquiryStatus status) {
    Instant latestEventAt = chatTimelineItemPort.findLatestCreatedAt(inquiry.getId());
    long unreadCount = chatTimelineItemPort.countUnread(inquiry.getId(), readerUserId, readAt);
    return InquiryListItem.from(inquiry, detail, status, unreadCount, latestEventAt);
  }

  private boolean isBuyerListTarget(Inquiry inquiry, InquiryStatus status) {
    if (!inquiry.isBuyerVisible()) {
      return false;
    }

    if (status == InquiryStatus.TRASH) {
      return inquiry.isBuyerTrashed();
    }

    return !inquiry.isBuyerTrashed() && (status == null || inquiry.getStatus() == status);
  }

  private boolean isSellerListTarget(Inquiry inquiry, InquiryStatus status) {
    if (!inquiry.isSellerVisible()) {
      return false;
    }

    if (status == InquiryStatus.TRASH) {
      return inquiry.isSellerTrashed();
    }

    return !inquiry.isSellerTrashed() && (status == null || inquiry.getStatus() == status);
  }

  private void ensureBuyerVisible(Inquiry inquiry) {
    if (!inquiry.isBuyerVisible()) {
      throw new BaseException(ChatErrorCode.CHAT_INQUIRY_NOT_FOUND);
    }
  }

  private void ensureSellerVisible(Inquiry inquiry) {
    if (!inquiry.isSellerVisible()) {
      throw new BaseException(ChatErrorCode.CHAT_INQUIRY_NOT_FOUND);
    }
  }

  private Comparator<InquiryListItem> byLatestEvent() {
    return Comparator.comparing(InquiryListItem::latestEventAt).reversed();
  }
}
