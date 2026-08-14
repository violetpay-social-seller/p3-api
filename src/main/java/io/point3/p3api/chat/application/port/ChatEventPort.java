package io.point3.p3api.chat.application.port;

import io.point3.p3api.chat.domain.entity.ChatEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ChatEventPort {

  ChatEvent save(ChatEvent chatEvent);

  List<ChatEvent> findTimeline(UUID inquiryId, Instant cursorCreatedAt, UUID cursorId, int limit);
}
