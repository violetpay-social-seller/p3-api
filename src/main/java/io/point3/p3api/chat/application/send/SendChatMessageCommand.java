package io.point3.p3api.chat.application.send;

import java.util.UUID;

public record SendChatMessageCommand(UUID chatRoomId, UUID senderId, String content) {}
