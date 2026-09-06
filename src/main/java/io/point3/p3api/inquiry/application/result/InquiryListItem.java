package io.point3.p3api.inquiry.application.result;

import io.point3.p3api.chat.domain.type.ChatTimelineItemType;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.inquiry.domain.type.InquiryStatus;
import java.time.Instant;
import java.util.UUID;

public record InquiryListItem(
    InquiryChatDetail detail,
    InquiryStatus status,
    long unreadCount,
    Instant latestEventAt,
    LatestEvent latestEvent,
    LatestOrderFormSubmission latestOrderFormSubmission) {
  public UUID inquiryId() {
    return detail.inquiryId();
  }

  public static InquiryListItem from(
      Inquiry inquiry, InquiryChatDetail detail, long unreadCount, Instant latestEventAt) {
    return from(inquiry, detail, inquiry.getStatus(), unreadCount, latestEventAt, null, null);
  }

  public static InquiryListItem from(
      Inquiry inquiry,
      InquiryChatDetail detail,
      InquiryStatus status,
      long unreadCount,
      Instant latestEventAt) {
    return from(inquiry, detail, status, unreadCount, latestEventAt, null, null);
  }

  public static InquiryListItem from(
      Inquiry inquiry,
      InquiryChatDetail detail,
      InquiryStatus status,
      long unreadCount,
      Instant latestEventAt,
      LatestEvent latestEvent,
      LatestOrderFormSubmission latestOrderFormSubmission) {
    return new InquiryListItem(
        detail,
        status,
        unreadCount,
        latestEventAt == null ? inquiry.getCreatedAt() : latestEventAt,
        latestEvent,
        latestOrderFormSubmission);
  }

  public record LatestEvent(
      UUID eventId,
      UUID referenceId,
      ChatTimelineItemType type,
      UUID senderUserId,
      String content,
      Instant createdAt) {}

  public record LatestOrderFormSubmission(UUID submissionId, Instant submittedAt) {}
}
