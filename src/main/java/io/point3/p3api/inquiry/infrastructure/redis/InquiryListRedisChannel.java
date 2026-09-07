package io.point3.p3api.inquiry.infrastructure.redis;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** 문의 목록 갱신 Redis Pub/Sub 채널을 정의한다. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InquiryListRedisChannel {

  public static final String UPDATES = "inquiry:list-updates";
}
