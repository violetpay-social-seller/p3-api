package io.point3.p3api.inquiry.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderFormSubmissionTest {

  @Test
  @DisplayName("판매자 확인 시각은 최초 확인 시각으로 멱등 기록된다")
  void recordsFirstSellerViewedAt() {
    OrderFormSubmission submission = OrderFormSubmission.create(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        LocalDate.of(2026, 9, 11),
        LocalTime.of(14, 0),
        "[]",
        null,
        true);
    Instant firstViewedAt = Instant.parse("2026-09-11T05:00:00Z");
    Instant secondViewedAt = Instant.parse("2026-09-11T06:00:00Z");

    submission.markSellerViewed(firstViewedAt);
    submission.markSellerViewed(secondViewedAt);

    assertTrue(submission.isSellerViewed());
    assertEquals(firstViewedAt, submission.getSellerViewedAt());
  }
}
