package io.point3.p3api.inquiry.controller.response;

import io.point3.p3api.inquiry.application.result.InquiryListItem;
import io.point3.p3api.inquiry.domain.type.InquiryStatus;
import java.time.Instant;
import java.util.UUID;

public record InquiryListItemResponse(
    UUID inquiryId,
    UUID storeId,
    InquiryStatus status,
    String storeName,
    String storeSlug,
    InquiryChatDetailResponse.ParticipantResponse participant,
    long unreadCount,
    Instant latestEventAt,
    Instant myLastReadAt,
    Instant createdAt) {

  public static InquiryListItemResponse from(InquiryListItem item) {
    var detail = InquiryChatDetailResponse.from(item.detail());
    return new InquiryListItemResponse(
        detail.inquiryId(),
        detail.storeId(),
        item.status(),
        detail.storeName(),
        detail.storeSlug(),
        detail.participant(),
        item.unreadCount(),
        item.latestEventAt(),
        detail.myLastReadAt(),
        detail.createdAt());
  }
}
