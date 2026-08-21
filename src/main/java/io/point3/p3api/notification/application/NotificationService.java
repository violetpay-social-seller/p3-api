package io.point3.p3api.notification.application;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.NotificationErrorCode;
import io.point3.p3api.notification.application.port.NotificationPersistencePort;
import io.point3.p3api.notification.application.query.NotificationQueryUseCase;
import io.point3.p3api.notification.application.result.NotificationResult;
import io.point3.p3api.notification.domain.entity.Notification;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService implements NotificationQueryUseCase {
  private final NotificationPersistencePort notificationPersistencePort;

  @Override
  @Transactional(readOnly = true)
  public List<NotificationResult> getNotifications(UUID userId) {
    return notificationPersistencePort.findAllByUserId(userId).stream()
        .map(NotificationResult::from)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public NotificationResult getNotification(UUID notificationId, UUID userId) {
    return NotificationResult.from(findNotification(notificationId, userId));
  }

  @Override
  public NotificationResult read(UUID notificationId, UUID userId) {
    Notification notification = findNotification(notificationId, userId);
    if (notification.getReadAt() == null) {
      notification.read(Instant.now());
    }
    return NotificationResult.from(notification);
  }

  @Override
  @Transactional(readOnly = true)
  public long getUnreadCount(UUID userId) {
    return notificationPersistencePort.countUnreadByUserId(userId);
  }

  private Notification findNotification(UUID notificationId, UUID userId) {
    return notificationPersistencePort
        .findByIdAndUserId(notificationId, userId)
        .orElseThrow(() -> new BaseException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));
  }
}
