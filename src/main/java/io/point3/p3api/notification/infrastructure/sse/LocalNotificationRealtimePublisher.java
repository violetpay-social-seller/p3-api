package io.point3.p3api.notification.infrastructure.sse;

import io.point3.p3api.notification.application.realtime.NotificationCreatedEvent;
import io.point3.p3api.notification.application.realtime.NotificationRealtimePublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** local-scenario 환경에서 저장 완료 알림을 즉시 SSE로 전달한다. */
@Component
@Profile("local-scenario")
@RequiredArgsConstructor
public class LocalNotificationRealtimePublisher implements NotificationRealtimePublisherPort {
  private final NotificationSseConnectionRegistry notificationSseConnectionRegistry;

  @Override
  public void publish(NotificationCreatedEvent event) {
    notificationSseConnectionRegistry.send(event.userId(), event.notification());
  }
}
