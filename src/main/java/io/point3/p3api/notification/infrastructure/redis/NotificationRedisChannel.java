package io.point3.p3api.notification.infrastructure.redis;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NotificationRedisChannel {
  public static final String CREATED = "notification:created";
}
