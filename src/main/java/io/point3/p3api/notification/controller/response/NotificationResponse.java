package io.point3.p3api.notification.controller.response;

import io.point3.p3api.notification.application.result.NotificationResult;
import io.point3.p3api.notification.domain.type.NotificationReferenceType;
import io.point3.p3api.notification.domain.type.NotificationType;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
    UUID id,
    NotificationType type,
    NotificationReferenceType referenceType,
    UUID referenceId,
    String title,
    String body,
    Instant readAt,
    Instant createdAt) {
  public static NotificationResponse from(NotificationResult result) {
    return new NotificationResponse(
        result.id(),
        result.type(),
        result.referenceType(),
        result.referenceId(),
        result.title(),
        result.body(),
        result.readAt(),
        result.createdAt());
  }
}
