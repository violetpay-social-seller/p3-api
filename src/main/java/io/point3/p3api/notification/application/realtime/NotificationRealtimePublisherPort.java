package io.point3.p3api.notification.application.realtime;

/** 저장 완료 알림을 현재 인스턴스의 SSE 연결로 전파한다. */
public interface NotificationRealtimePublisherPort {
  void publish(NotificationCreatedEvent event);
}
