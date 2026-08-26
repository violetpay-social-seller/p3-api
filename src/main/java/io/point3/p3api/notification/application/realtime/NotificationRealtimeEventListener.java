package io.point3.p3api.notification.application.realtime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** 알림 저장 트랜잭션이 커밋된 경우에만 SSE 전파를 시작한다. */
@Component
@RequiredArgsConstructor
public class NotificationRealtimeEventListener {
  private final NotificationRealtimePublisherPort notificationRealtimePublisherPort;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void publish(NotificationCreatedEvent event) {
    notificationRealtimePublisherPort.publish(event);
  }
}
