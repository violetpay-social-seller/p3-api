package io.point3.p3api.local;

import io.point3.p3api.chat.application.port.ChatTimelineRealtimePublisherPort;
import io.point3.p3api.chat.application.realtime.ChatTimelineRealtimeEvent;
import io.point3.p3api.chat.controller.ChatStompDestination;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("local-scenario")
@RequiredArgsConstructor
public class LocalScenarioChatTimelinePublisher implements ChatTimelineRealtimePublisherPort {

  private final SimpMessagingTemplate messagingTemplate;

  @Override
  public void publish(ChatTimelineRealtimeEvent event) {
    messagingTemplate.convertAndSend(
        ChatStompDestination.topicDestination(event.inquiryId()), event.payload());
  }
}
