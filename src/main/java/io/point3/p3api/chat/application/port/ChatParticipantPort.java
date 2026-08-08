package io.point3.p3api.chat.application.port;

import io.point3.p3api.chat.domain.entity.ChatParticipant;
import java.util.UUID;

public interface ChatParticipantPort {

    ChatParticipant save(ChatParticipant chatParticipant);

    boolean existsByChatRoomIdAndUserId(UUID chatRoomId, UUID userId);
}
