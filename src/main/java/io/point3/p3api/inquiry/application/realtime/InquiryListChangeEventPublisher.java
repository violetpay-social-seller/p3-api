package io.point3.p3api.inquiry.application.realtime;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/** 문의 목록 갱신 이벤트 발행 입구 */
@Component
@RequiredArgsConstructor
public class InquiryListChangeEventPublisher {

  private final ApplicationEventPublisher applicationEventPublisher;

  public void publishInquiryChanged(UUID inquiryId) {
    applicationEventPublisher.publishEvent(new InquiryListChangedEvent(inquiryId));
  }

  public void publishReaderChanged(UUID inquiryId, UUID readerUserId) {
    applicationEventPublisher.publishEvent(
        new InquiryListReaderChangedEvent(inquiryId, readerUserId));
  }
}
