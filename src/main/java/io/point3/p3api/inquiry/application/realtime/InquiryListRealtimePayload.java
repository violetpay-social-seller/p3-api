package io.point3.p3api.inquiry.application.realtime;

import io.point3.p3api.inquiry.domain.type.InquiryStatus;
import java.time.Instant;
import java.util.UUID;

public record InquiryListRealtimePayload(
    String type, UUID inquiryId, long unreadCount, Instant latestEventAt, InquiryStatus status) {

  private static final String INQUIRY_UPDATED = "INQUIRY_UPDATED";

  public static InquiryListRealtimePayload inquiryUpdated(
      UUID inquiryId, long unreadCount, Instant latestEventAt, InquiryStatus status) {
    return new InquiryListRealtimePayload(
        INQUIRY_UPDATED, inquiryId, unreadCount, latestEventAt, status);
  }
}
