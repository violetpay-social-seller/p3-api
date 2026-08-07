package io.point3.p3api.chat.application.port;

import io.point3.p3api.chat.domain.entity.ChatMessage;

public interface ChatMessagePort {

    ChatMessage save(ChatMessage chatMessage);
}
