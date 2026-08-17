package io.point3.p3api.chat.infrastructure.redis;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** 채팅 Redis Pub/Sub 채널을 정의한다. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChatRedisChannel {

  public static final String MESSAGES = "chat:messages";
}
