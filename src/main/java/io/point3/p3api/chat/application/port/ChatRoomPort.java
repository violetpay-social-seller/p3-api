package io.point3.p3api.chat.application.port;

import io.point3.p3api.chat.domain.entity.ChatRoom;
import java.util.Optional;
import java.util.UUID;

public interface ChatRoomPort {

    ChatRoom save(ChatRoom chatRoom);

    Optional<ChatRoom> findById(UUID id);
}
