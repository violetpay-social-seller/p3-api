package io.point3.p3api.notification.application.port;

import io.point3.p3api.notification.domain.entity.Notification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationPersistencePort {
  Notification save(Notification notification);

  Optional<Notification> findByIdAndUserId(UUID notificationId, UUID userId);

  List<Notification> findAllByUserId(UUID userId);

  long countUnreadByUserId(UUID userId);
}
