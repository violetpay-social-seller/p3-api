package io.point3.p3api.inquiry.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.point3.p3api.inquiry.domain.type.InquiryStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InquiryTest {

  @Test
  @DisplayName("새 문의방은 접수대기 상태로 생성되고 읽음 시각을 역할별로 기록한다")
  void createsWaitingInquiryAndRecordsReadTimestamps() {
    Inquiry inquiry = Inquiry.create(UUID.randomUUID(), UUID.randomUUID());
    Instant buyerReadAt = Instant.parse("2026-08-25T00:00:00Z");
    Instant sellerReadAt = Instant.parse("2026-08-25T01:00:00Z");

    inquiry.markBuyerRead(buyerReadAt);
    inquiry.markSellerRead(sellerReadAt);

    assertEquals(InquiryStatus.WAITING, inquiry.getStatus());
    assertEquals(buyerReadAt, inquiry.getBuyerLastReadAt());
    assertEquals(sellerReadAt, inquiry.getSellerLastReadAt());
  }

  @Test
  @DisplayName("결제완료 또는 픽업완료 상담은 판매자 조회로 상담중 상태로 역행하지 않는다")
  void doesNotMovePaidOrPickedUpInquiryBackToInProgress() {
    Inquiry paidInquiry = Inquiry.create(UUID.randomUUID(), UUID.randomUUID());
    Inquiry pickedUpInquiry = Inquiry.create(UUID.randomUUID(), UUID.randomUUID());

    paidInquiry.markPaid();
    pickedUpInquiry.markPickedUp();
    paidInquiry.markInProgressOnSellerReview();
    pickedUpInquiry.markInProgressOnSellerReview();

    assertEquals(InquiryStatus.PAID, paidInquiry.getStatus());
    assertEquals(InquiryStatus.PICKED_UP, pickedUpInquiry.getStatus());
  }
}
