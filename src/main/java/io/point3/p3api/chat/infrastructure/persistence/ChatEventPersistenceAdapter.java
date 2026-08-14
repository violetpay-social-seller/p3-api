package io.point3.p3api.chat.infrastructure.persistence;

import io.point3.p3api.chat.application.port.ChatEventPort;
import io.point3.p3api.chat.domain.entity.ChatEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatEventPersistenceAdapter implements ChatEventPort {

  private final ChatEventJpaRepository chatEventJpaRepository;

  @Override
  public ChatEvent save(ChatEvent chatEvent) {
    return chatEventJpaRepository.save(chatEvent);
  }

  @Override
  public List<ChatEvent> findTimeline(
      UUID inquiryId, Instant cursorCreatedAt, UUID cursorId, int limit) {
    return chatEventJpaRepository.findTimeline(
        inquiryId, cursorCreatedAt, cursorId, PageRequest.of(0, limit));
  }
}
