package io.point3.p3api.notification.application.query;

import io.point3.p3api.notification.application.result.NotificationResult;
import java.util.List;
import java.util.UUID;

public interface NotificationQueryUseCase {
  List<NotificationResult> getNotifications(UUID userId);

  NotificationResult getNotification(UUID notificationId, UUID userId);

  NotificationResult read(UUID notificationId, UUID userId);

  void readAll(UUID userId);

  long getUnreadCount(UUID userId);
}
