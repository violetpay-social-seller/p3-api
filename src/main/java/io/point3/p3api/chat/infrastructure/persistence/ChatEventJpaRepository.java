package io.point3.p3api.chat.infrastructure.persistence;

import io.point3.p3api.chat.domain.entity.ChatEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatEventJpaRepository extends JpaRepository<ChatEvent, UUID> {

  @Query(
      """
      select chatEvent
      from ChatEvent chatEvent
      where chatEvent.inquiryId = :inquiryId
        and (
          :cursorCreatedAt is null
          or chatEvent.createdAt < :cursorCreatedAt
          or (chatEvent.createdAt = :cursorCreatedAt and chatEvent.id < :cursorId)
        )
      order by chatEvent.createdAt desc, chatEvent.id desc
      """)
  List<ChatEvent> findTimeline(
      @Param("inquiryId") UUID inquiryId,
      @Param("cursorCreatedAt") Instant cursorCreatedAt,
      @Param("cursorId") UUID cursorId,
      Pageable pageable);
}
