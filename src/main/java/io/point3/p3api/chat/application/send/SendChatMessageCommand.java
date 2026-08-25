package io.point3.p3api.chat.application.send;

import java.util.List;
import java.util.UUID;

public record SendChatMessageCommand(
    UUID inquiryId, UUID senderUserId, String content, List<UUID> assetIds) {

  public SendChatMessageCommand {
    assetIds = assetIds == null ? List.of() : List.copyOf(assetIds);
  }

  public static SendChatMessageCommand of(UUID inquiryId, UUID senderUserId, String content) {
    return new SendChatMessageCommand(inquiryId, senderUserId, content, List.of());
  }

  public static SendChatMessageCommand of(
      UUID inquiryId, UUID senderUserId, String content, List<UUID> assetIds) {
    return new SendChatMessageCommand(inquiryId, senderUserId, content, assetIds);
  }
}
