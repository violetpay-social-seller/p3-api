package io.point3.p3api.chat.application.send;

import io.point3.p3api.chat.domain.entity.ChatMessage;
import io.point3.p3api.chat.domain.entity.ChatTimelineItem;

public record SendChatMessageResult(ChatMessage chatMessage, ChatTimelineItem chatTimelineItem) {}
