package io.point3.p3api.notification.application.create;

import io.point3.p3api.notification.application.result.NotificationResult;

public interface NotificationCreateUseCase {
  NotificationResult create(CreateNotificationCommand command);
}
