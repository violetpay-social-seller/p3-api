package io.point3.p3api.notification.infrastructure.persistence;

import io.point3.p3api.notification.application.port.NotificationPersistencePort;
import io.point3.p3api.notification.domain.entity.Notification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class NotificationPersistenceAdapter implements NotificationPersistencePort {
  private final NotificationJpaRepository notificationJpaRepository;

  @Override
  public Notification save(Notification notification) {
    return notificationJpaRepository.save(notification);
  }

  @Override
  public Optional<Notification> findByIdAndUserId(UUID notificationId, UUID userId) {
    return notificationJpaRepository.findByIdAndUserId(notificationId, userId);
  }

  @Override
  public List<Notification> findAllByUserId(UUID userId) {
    return notificationJpaRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
  }

  @Override
  public long countUnreadByUserId(UUID userId) {
    return notificationJpaRepository.countByUserIdAndReadAtIsNull(userId);
  }
}
