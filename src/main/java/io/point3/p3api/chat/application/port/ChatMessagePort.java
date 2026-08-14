package io.point3.p3api.chat.application.port;

import io.point3.p3api.chat.domain.entity.ChatMessage;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ChatMessagePort {

  ChatMessage save(ChatMessage chatMessage);

  List<ChatMessage> findAllById(Collection<UUID> messageIds);
}
