package io.point3.p3api.notification.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.point3.p3api.notification.domain.type.NotificationReferenceType;
import io.point3.p3api.notification.domain.type.NotificationType;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotificationTest {

  @Test
  @DisplayName("알림은 참조 대상과 함께 생성되고 읽음 시각을 기록한다")
  void createsAndReadsNotification() {
    UUID referenceId = UUID.randomUUID();
    Notification notification = Notification.create(
        UUID.randomUUID(),
        NotificationType.PAYMENT_COMPLETED,
        NotificationReferenceType.ORDER,
        referenceId,
        "결제 완료",
        "결제가 완료되었습니다.");
    Instant readAt = Instant.parse("2026-08-25T00:00:00Z");

    notification.read(readAt);

    assertEquals(NotificationType.PAYMENT_COMPLETED, notification.getType());
    assertEquals(NotificationReferenceType.ORDER, notification.getReferenceType());
    assertEquals(referenceId, notification.getReferenceId());
    assertEquals(readAt, notification.getReadAt());
  }

  @Test
  @DisplayName("알림 생성에는 사용자, 유형, 제목, 본문이 필요하다")
  void requiresEssentialFields() {
    assertThrows(
        NullPointerException.class,
        () -> Notification.create(null, NotificationType.INQUIRY_CREATED, null, null, "제목", "본문"));
    assertThrows(
        NullPointerException.class,
        () -> Notification.create(UUID.randomUUID(), null, null, null, "제목", "본문"));
    assertThrows(
        NullPointerException.class,
        () -> Notification.create(
            UUID.randomUUID(), NotificationType.INQUIRY_CREATED, null, null, null, "본문"));
  }
}
