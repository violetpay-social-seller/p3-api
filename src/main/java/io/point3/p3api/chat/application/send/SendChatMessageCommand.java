package io.point3.p3api.chat.application.send;

import java.util.UUID;

public record SendChatMessageCommand(UUID inquiryId, UUID senderUserId, String content) {}
