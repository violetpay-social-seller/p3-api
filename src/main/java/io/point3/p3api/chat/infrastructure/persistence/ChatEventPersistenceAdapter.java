package io.point3.p3api.chat.infrastructure.persistence;

import io.point3.p3api.chat.application.port.ChatEventPort;
import io.point3.p3api.chat.domain.entity.ChatEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatEventPersistenceAdapter implements ChatEventPort {

  private final ChatEventJpaRepository chatEventJpaRepository;

  @Override
  public ChatEvent save(ChatEvent chatEvent) {
    return chatEventJpaRepository.save(chatEvent);
  }
}
