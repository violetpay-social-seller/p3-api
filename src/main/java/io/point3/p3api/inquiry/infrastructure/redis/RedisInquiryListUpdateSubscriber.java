package io.point3.p3api.inquiry.infrastructure.redis;

import io.point3.p3api.inquiry.controller.InquiryListStompDestination;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** 모든 App 인스턴스에서 Redis 문의 목록 갱신 이벤트를 수신한다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisInquiryListUpdateSubscriber implements MessageListener {

  private final InquiryListRedisEventSerializer inquiryListRedisEventSerializer;
  private final SimpMessagingTemplate messagingTemplate;

  @Override
  public void onMessage(Message message, byte[] pattern) {
    try {
      InquiryListRedisEvent event = inquiryListRedisEventSerializer.deserialize(message.getBody());
      messagingTemplate.convertAndSend(
          InquiryListStompDestination.topicDestination(event.userId()), event.payload());
      log.debug(
          "Forwarded inquiry list update to local STOMP topic: userId={}, inquiryId={}",
          event.userId(),
          event.payload().inquiryId());
    } catch (RuntimeException e) {
      log.error("Failed to process inquiry list update event", e);
    }
  }
}
