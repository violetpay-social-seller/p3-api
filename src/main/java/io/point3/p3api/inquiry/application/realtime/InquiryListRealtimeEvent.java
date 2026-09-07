package io.point3.p3api.inquiry.application.realtime;

import java.util.UUID;

public record InquiryListRealtimeEvent(UUID userId, InquiryListRealtimePayload payload) {}
