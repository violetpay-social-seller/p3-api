package io.point3.p3api.inquiry.application.result;

import java.time.Instant;
import java.util.UUID;

public record InquiryListItem(InquiryChatDetail detail, long unreadCount, Instant latestEventAt) {
  public UUID inquiryId() {
    return detail.inquiryId();
  }
}
