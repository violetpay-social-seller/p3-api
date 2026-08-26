package io.point3.p3api.notification.infrastructure.redis;

import io.point3.p3api.notification.application.result.NotificationResult;
import java.util.UUID;

/** Redis로 전파하는 수신 사용자와 저장 완료 알림이다. */
public record NotificationRedisEvent(UUID userId, NotificationResult notification) {}
