package io.point3.p3api.inquiry.controller.response;

import io.point3.p3api.chat.application.timeline.result.ChatTimelinePage;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChatTimelinePageResponse(
    List<ChatTimelineItemResponse> items,
    boolean hasNext,
    Instant nextCursorCreatedAt,
    UUID nextCursorId) {

  public ChatTimelinePageResponse {
    items = List.copyOf(items);
  }

  @Override
  public List<ChatTimelineItemResponse> items() {
    return List.copyOf(items);
  }

  public static ChatTimelinePageResponse from(ChatTimelinePage page) {
    return new ChatTimelinePageResponse(
        page.items().stream().map(ChatTimelineItemResponse::from).toList(),
        page.hasNext(),
        page.nextCursorCreatedAt(),
        page.nextCursorId());
  }
}
