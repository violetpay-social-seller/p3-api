package io.point3.p3api.chat.infrastructure.persistence;

import io.point3.p3api.chat.domain.entity.ChatTimelineItem;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatTimelineItemJpaRepository extends JpaRepository<ChatTimelineItem, UUID> {

  @Query("""
      select chatTimelineItem
      from ChatTimelineItem chatTimelineItem
      where chatTimelineItem.inquiryId = :inquiryId
        and (
          :cursorCreatedAt is null
          or chatTimelineItem.createdAt < :cursorCreatedAt
          or (chatTimelineItem.createdAt = :cursorCreatedAt and chatTimelineItem.id < :cursorId)
        )
      order by chatTimelineItem.createdAt desc, chatTimelineItem.id desc
      """)
  List<ChatTimelineItem> findTimeline(
      @Param("inquiryId") UUID inquiryId,
      @Param("cursorCreatedAt") Instant cursorCreatedAt,
      @Param("cursorId") UUID cursorId,
      Pageable pageable);
}
