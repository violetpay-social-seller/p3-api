package io.point3.p3api.inquiry.infrastructure.redis;

import io.point3.p3api.inquiry.application.realtime.InquiryListRealtimePayload;
import java.util.UUID;

public record InquiryListRedisEvent(UUID userId, InquiryListRealtimePayload payload) {}
