package io.point3.p3api.notification.application.realtime;

import io.point3.p3api.notification.application.result.NotificationResult;
import java.util.UUID;

/** 커밋 후 실시간 전파할 저장 완료 알림 이벤트다. */
public record NotificationCreatedEvent(UUID userId, NotificationResult notification) {}
