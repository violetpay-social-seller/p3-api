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
    if (cursorCreatedAt == null) {
      return chatTimelineItemJpaRepository.findTimeline(inquiryId, PageRequest.of(0, limit));
    }

    return chatTimelineItemJpaRepository.findTimeline(
        inquiryId, cursorCreatedAt, cursorId, PageRequest.of(0, limit));
  }

  @Override
  public long countUnread(UUID inquiryId, UUID readerUserId, Instant readAt) {
    if (readAt == null) {
      return chatTimelineItemJpaRepository.countUnread(inquiryId, readerUserId);
    }

    return chatTimelineItemJpaRepository.countUnread(inquiryId, readerUserId, readAt);
  }

  @Override
  public Instant findLatestCreatedAt(UUID inquiryId) {
    return chatTimelineItemJpaRepository.findLatestCreatedAt(inquiryId);
  }
}
