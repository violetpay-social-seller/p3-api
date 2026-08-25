package io.point3.p3api.chat.application.timeline.result;

import io.point3.p3api.chat.domain.entity.ChatMessage;
import io.point3.p3api.chat.domain.entity.ChatMessageAsset;
import io.point3.p3api.chat.domain.entity.ChatTimelineItem;
import io.point3.p3api.chat.domain.type.ChatTimelineItemType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChatTimelineItemResult(
    UUID eventId,
    ChatTimelineItemType type,
    UUID senderUserId,
    Instant createdAt,
    String content,
    List<UUID> assetIds) {

  public ChatTimelineItemResult {
    assetIds = assetIds == null ? List.of() : List.copyOf(assetIds);
  }

  public static ChatTimelineItemResult from(
      ChatTimelineItem chatTimelineItem, ChatMessage chatMessage) {
    return from(chatTimelineItem, chatMessage, List.of());
  }

  public static ChatTimelineItemResult from(
      ChatTimelineItem chatTimelineItem,
      ChatMessage chatMessage,
      List<ChatMessageAsset> chatMessageAssets) {
    return new ChatTimelineItemResult(
        chatTimelineItem.getId(),
        chatTimelineItem.getType(),
        chatTimelineItem.getSenderUserId(),
        chatTimelineItem.getCreatedAt(),
        chatMessage == null ? null : chatMessage.getContent(),
        chatMessageAssets.stream().map(ChatMessageAsset::getAssetId).toList());
  }
}
