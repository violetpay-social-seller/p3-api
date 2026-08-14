package io.point3.p3api.chat.application.timeline.query;

import java.time.Instant;
import java.util.UUID;

public record ChatTimelineQuery(Instant cursorCreatedAt, UUID cursorId, Integer size) {}
