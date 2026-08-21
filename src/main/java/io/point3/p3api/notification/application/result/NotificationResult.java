package io.point3.p3api.notification.application.result;

import io.point3.p3api.notification.domain.entity.Notification;
import io.point3.p3api.notification.domain.type.NotificationReferenceType;
import io.point3.p3api.notification.domain.type.NotificationType;
import java.time.Instant;
import java.util.UUID;

public record NotificationResult(
    UUID id,
    NotificationType type,
    NotificationReferenceType referenceType,
    UUID referenceId,
    String title,
    String body,
    Instant readAt,
    Instant createdAt) {
  public static NotificationResult from(Notification notification) {
    return new NotificationResult(
        notification.getId(),
        notification.getType(),
        notification.getReferenceType(),
        notification.getReferenceId(),
        notification.getTitle(),
        notification.getBody(),
        notification.getReadAt(),
        notification.getCreatedAt());
  }
}
