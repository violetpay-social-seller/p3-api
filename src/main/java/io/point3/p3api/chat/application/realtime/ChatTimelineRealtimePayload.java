package io.point3.p3api.chat.application.realtime;

import io.point3.p3api.chat.application.timeline.result.ChatTimelineItemResult;
import io.point3.p3api.chat.domain.type.ChatTimelineItemType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChatTimelineRealtimePayload(
    UUID eventId,
    UUID referenceId,
    ChatTimelineItemType type,
    UUID senderUserId,
    Instant createdAt,
    String content,
    List<UUID> assetIds) {

  public ChatTimelineRealtimePayload {
    assetIds = assetIds == null ? List.of() : List.copyOf(assetIds);
  }

  public static ChatTimelineRealtimePayload from(ChatTimelineItemResult item) {
    return new ChatTimelineRealtimePayload(
        item.eventId(),
        item.referenceId(),
        item.type(),
        item.senderUserId(),
        item.createdAt(),
        item.content(),
        item.assetIds());
  }

  @Override
  public List<UUID> assetIds() {
    return List.copyOf(assetIds);
  }
}
