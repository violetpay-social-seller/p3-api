package io.point3.p3api.inquiry.infrastructure.redis;

import io.point3.p3api.inquiry.application.realtime.InquiryListRealtimeEvent;
import io.point3.p3api.inquiry.application.realtime.InquiryListRealtimePublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** 다중 인스턴스에 문의 목록 갱신 이벤트를 전파한다. */
@Slf4j
@Component
@Profile("!local-scenario")
@RequiredArgsConstructor
public class RedisInquiryListUpdatePublisher implements InquiryListRealtimePublisherPort {

  private final StringRedisTemplate stringRedisTemplate;
  private final InquiryListRedisEventSerializer inquiryListRedisEventSerializer;

  @Override
  public void publish(InquiryListRealtimeEvent event) {
    try {
      stringRedisTemplate.convertAndSend(
          InquiryListRedisChannel.UPDATES,
          inquiryListRedisEventSerializer.serialize(
              new InquiryListRedisEvent(event.userId(), event.payload())));
    } catch (RuntimeException e) {
      log.error(
          "Failed to publish inquiry list update event: userId={}, inquiryId={}",
          event.userId(),
          event.payload().inquiryId(),
          e);
    }
  }
}
