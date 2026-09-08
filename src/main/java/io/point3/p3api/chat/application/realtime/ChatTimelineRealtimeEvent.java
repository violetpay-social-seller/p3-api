package io.point3.p3api.chat.application.realtime;

import java.util.UUID;

public record ChatTimelineRealtimeEvent(UUID inquiryId, ChatTimelineRealtimePayload payload) {}
