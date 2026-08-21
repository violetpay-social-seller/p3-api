package io.point3.p3api.notification.infrastructure.persistence;

import io.point3.p3api.notification.domain.entity.Notification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationJpaRepository extends JpaRepository<Notification, UUID> {
  Optional<Notification> findByIdAndUserId(UUID notificationId, UUID userId);
  List<Notification> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
  long countByUserIdAndReadAtIsNull(UUID userId);
}
