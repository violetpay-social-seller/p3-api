package io.point3.p3api.local;

import io.point3.p3api.inquiry.application.realtime.InquiryListRealtimeEvent;
import io.point3.p3api.inquiry.application.realtime.InquiryListRealtimePublisherPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local-scenario")
public class LocalScenarioInquiryListRealtimePublisher implements InquiryListRealtimePublisherPort {

  @Override
  public void publish(InquiryListRealtimeEvent event) {
    // local-scenario는 외부 Redis/STOMP 전파 생략
  }
}
