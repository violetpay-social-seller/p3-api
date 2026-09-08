package io.point3.p3api.chat.application.port;

import io.point3.p3api.chat.application.realtime.ChatTimelineRealtimeEvent;

/** 저장된 채팅 타임라인 항목을 실시간 전파 채널로 발행한다. */
public interface ChatTimelineRealtimePublisherPort {

  void publish(ChatTimelineRealtimeEvent event);
}
