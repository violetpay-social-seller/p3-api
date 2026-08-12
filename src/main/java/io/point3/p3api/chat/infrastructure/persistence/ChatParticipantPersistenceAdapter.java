package io.point3.p3api.chat.infrastructure.persistence;

import io.point3.p3api.chat.application.port.ChatParticipantPort;
import io.point3.p3api.chat.domain.entity.ChatParticipant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatParticipantPersistenceAdapter implements ChatParticipantPort {

  private final ChatParticipantJpaRepository chatParticipantJpaRepository;

  @Override
  public ChatParticipant save(ChatParticipant chatParticipant) {
    return chatParticipantJpaRepository.save(chatParticipant);
  }

  @Override
  public boolean existsByChatRoomIdAndUserId(UUID chatRoomId, UUID userId) {
    return chatParticipantJpaRepository.existsByChatRoomIdAndUserId(chatRoomId, userId);
  }
}
