package io.point3.p3api.chat.infrastructure.persistence;

import io.point3.p3api.chat.application.port.ChatMessagePort;
import io.point3.p3api.chat.domain.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatMessagePersistenceAdapter implements ChatMessagePort {

  private final ChatMessageJpaRepository chatMessageJpaRepository;

  @Override
  public ChatMessage save(ChatMessage chatMessage) {
    return chatMessageJpaRepository.save(chatMessage);
  }
}
