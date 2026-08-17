package io.point3.p3api.chat.application.send;

import java.util.UUID;

public record SendChatMessageCommand(UUID inquiryId, UUID senderUserId, String content) {

  public static SendChatMessageCommand of(UUID inquiryId, UUID senderUserId, String content) {
    return new SendChatMessageCommand(inquiryId, senderUserId, content);
  }
}
