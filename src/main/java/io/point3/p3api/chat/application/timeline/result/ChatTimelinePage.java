package io.point3.p3api.chat.application.timeline.result;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChatTimelinePage(
    List<ChatTimelineItemResult> items,
    boolean hasNext,
    Instant nextCursorCreatedAt,
    UUID nextCursorId) {}
