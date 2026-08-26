package io.point3.p3api.notification.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.point3.p3api.IntegrationTestSupport;
import io.point3.p3api.notification.application.create.CreateNotificationCommand;
import io.point3.p3api.notification.application.create.NotificationCreateUseCase;
import io.point3.p3api.notification.application.query.NotificationQueryUseCase;
import io.point3.p3api.notification.application.result.NotificationResult;
import io.point3.p3api.notification.domain.type.NotificationReferenceType;
import io.point3.p3api.notification.domain.type.NotificationType;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.infrastructure.persistence.UserJpaRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class NotificationServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private NotificationCreateUseCase notificationCreateUseCase;

  @Autowired
  private NotificationQueryUseCase notificationQueryUseCase;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Test
  void readsAllNotificationsForCurrentUser() {
    User user = userJpaRepository.saveAndFlush(User.create(
        UUID.randomUUID().toString(), uniqueEmail("notification"), "알림 사용자", UserRole.BUYER));
    UUID orderId = UUID.randomUUID();
    NotificationResult created = notificationCreateUseCase.create(new CreateNotificationCommand(
        user.getId(),
        NotificationType.PAYMENT_COMPLETED,
        NotificationReferenceType.ORDER,
        orderId,
        "결제가 완료되었습니다.",
        "주문 내역을 확인해 주세요."));

    assertEquals(1, notificationQueryUseCase.getUnreadCount(user.getId()));
    assertEquals(NotificationReferenceType.ORDER, created.referenceType());
    assertEquals(orderId, created.referenceId());

    notificationQueryUseCase.readAll(user.getId());

    NotificationResult read = notificationQueryUseCase.getNotification(created.id(), user.getId());
    assertNotNull(read.readAt());
    assertEquals(0, notificationQueryUseCase.getUnreadCount(user.getId()));
  }
}
