package io.point3.p3api.chat.infrastructure.persistence;

import io.point3.p3api.chat.application.port.ChatTimelineItemPort;
import io.point3.p3api.chat.domain.entity.ChatTimelineItem;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatTimelineItemPersistenceAdapter implements ChatTimelineItemPort {

  private final ChatTimelineItemJpaRepository chatTimelineItemJpaRepository;

  @Override
  public ChatTimelineItem save(ChatTimelineItem chatTimelineItem) {
    return chatTimelineItemJpaRepository.save(chatTimelineItem);
  }

  @Override
  public List<ChatTimelineItem> findTimeline(
      UUID inquiryId, Instant cursorCreatedAt, UUID cursorId, int limit) {
    return chatTimelineItemJpaRepository.findTimeline(
        inquiryId, cursorCreatedAt, cursorId, PageRequest.of(0, limit));
  }
}
