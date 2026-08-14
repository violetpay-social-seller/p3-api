package io.point3.p3api.chat.application.send;

import io.point3.p3api.chat.domain.entity.ChatEvent;
import io.point3.p3api.chat.domain.entity.ChatMessage;

public record SendChatMessageResult(ChatMessage chatMessage, ChatEvent chatEvent) {}
