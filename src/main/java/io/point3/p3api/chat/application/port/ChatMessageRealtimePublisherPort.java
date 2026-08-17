package io.point3.p3api.chat.application.port;

import io.point3.p3api.chat.application.send.SendChatMessageResult;

/** 저장된 채팅 메시지를 실시간 전파 채널로 발행한다. */
public interface ChatMessageRealtimePublisherPort {

  void publish(SendChatMessageResult result);
}
