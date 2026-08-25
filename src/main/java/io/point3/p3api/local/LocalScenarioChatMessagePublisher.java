package io.point3.p3api.local;

import io.point3.p3api.chat.application.port.ChatMessageRealtimePublisherPort;
import io.point3.p3api.chat.application.send.SendChatMessageResult;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local-scenario")
public class LocalScenarioChatMessagePublisher implements ChatMessageRealtimePublisherPort {

  @Override
  public void publish(SendChatMessageResult result) {}
}
