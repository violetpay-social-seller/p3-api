package io.point3.p3api.chat.infrastructure.persistence;

import io.point3.p3api.chat.application.port.ChatRoomPort;
import io.point3.p3api.chat.domain.entity.ChatRoom;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatRoomPersistenceAdapter implements ChatRoomPort {

  private final ChatRoomJpaRepository chatRoomJpaRepository;

  @Override
  public ChatRoom save(ChatRoom chatRoom) {
    return chatRoomJpaRepository.save(chatRoom);
  }

  @Override
  public Optional<ChatRoom> findById(UUID id) {
    return chatRoomJpaRepository.findById(id);
  }
}
