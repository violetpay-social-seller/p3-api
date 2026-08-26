package io.point3.p3api.notification.application.create;

import io.point3.p3api.notification.domain.type.NotificationReferenceType;
import io.point3.p3api.notification.domain.type.NotificationType;
import java.util.UUID;

public record CreateNotificationCommand(
    UUID userId,
    NotificationType type,
    NotificationReferenceType referenceType,
    UUID referenceId,
    String title,
    String body) {}
