package io.point3.p3api.chat.application.realtime;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/** 채팅 타임라인 생성 이벤트 발행 입구 */
@Component
@RequiredArgsConstructor
public class ChatTimelineRealtimeEventPublisher {

  private final ApplicationEventPublisher applicationEventPublisher;

  public void publishTimelineItemCreated(UUID eventId) {
    applicationEventPublisher.publishEvent(new ChatTimelineItemCreatedEvent(eventId));
  }
}
