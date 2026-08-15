package io.point3.p3api.inquiry.application;

import io.point3.p3api.chat.application.port.ChatEventPort;
import io.point3.p3api.chat.domain.entity.ChatEvent;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 주문서/확인서/결제요청 과 같은 카드 이벤트 발행
 */
@Component
@RequiredArgsConstructor
public class InquiryTimelineEventPublisher {

  private final ChatEventPort chatEventPort;

  public void publishOrderFormSubmission(UUID inquiryId, UUID buyerUserId, UUID submissionId) {
    chatEventPort.save(ChatEvent.orderFormSubmission(inquiryId, buyerUserId, submissionId));
  }

  public void publishOrderConfirmation(
      UUID inquiryId, UUID senderUserId, UUID orderConfirmationId) {
    chatEventPort.save(
        ChatEvent.orderConfirmation(inquiryId, senderUserId, orderConfirmationId));
  }

  public void publishPaymentRequest(UUID inquiryId, UUID senderUserId, UUID paymentRequestId) {
    chatEventPort.save(ChatEvent.paymentRequest(inquiryId, senderUserId, paymentRequestId));
  }
}
