package io.point3.p3api.chat.application.port;

import io.point3.p3api.chat.domain.entity.ChatTimelineItem;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ChatTimelineItemPort {

  ChatTimelineItem save(ChatTimelineItem chatTimelineItem);

  List<ChatTimelineItem> findTimeline(
      UUID inquiryId, Instant cursorCreatedAt, UUID cursorId, int limit);
}
