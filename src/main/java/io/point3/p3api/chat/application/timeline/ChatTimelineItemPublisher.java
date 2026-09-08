package io.point3.p3api.chat.application.timeline;

import io.point3.p3api.chat.application.port.ChatTimelineItemPort;
import io.point3.p3api.chat.application.realtime.ChatTimelineRealtimeEventPublisher;
import io.point3.p3api.chat.domain.entity.ChatTimelineItem;
import io.point3.p3api.inquiry.application.realtime.InquiryListChangeEventPublisher;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 채팅방에 표시할 메시지와 도메인 카드 타임라인 항목을 기록한다. */
@Component
@RequiredArgsConstructor
public class ChatTimelineItemPublisher {

  private final ChatTimelineItemPort chatTimelineItemPort;
  private final InquiryListChangeEventPublisher inquiryListChangeEventPublisher;
  private final ChatTimelineRealtimeEventPublisher chatTimelineRealtimeEventPublisher;

  public ChatTimelineItem publishMessage(UUID inquiryId, UUID senderUserId, UUID chatMessageId) {
    return save(ChatTimelineItem.message(inquiryId, senderUserId, chatMessageId));
  }

  public ChatTimelineItem publishOrderFormSubmission(
      UUID inquiryId, UUID buyerUserId, UUID submissionId) {
    return save(ChatTimelineItem.orderFormSubmission(inquiryId, buyerUserId, submissionId));
  }

  public ChatTimelineItem publishOrderConfirmation(
      UUID inquiryId, UUID senderUserId, UUID orderConfirmationId) {
    return save(ChatTimelineItem.orderConfirmation(inquiryId, senderUserId, orderConfirmationId));
  }

  public ChatTimelineItem publishOrderConfirmationRevisionRequest(
      UUID inquiryId, UUID buyerUserId, UUID orderConfirmationId) {
    return save(ChatTimelineItem.orderConfirmationRevisionRequest(
        inquiryId, buyerUserId, orderConfirmationId));
  }

  public ChatTimelineItem publishPaymentCompleted(UUID inquiryId, UUID buyerUserId, UUID orderId) {
    return save(ChatTimelineItem.paymentCompleted(inquiryId, buyerUserId, orderId));
  }

  private ChatTimelineItem save(ChatTimelineItem item) {
    ChatTimelineItem savedItem = chatTimelineItemPort.save(item);
    inquiryListChangeEventPublisher.publishInquiryChanged(savedItem.getInquiryId());
    chatTimelineRealtimeEventPublisher.publishTimelineItemCreated(savedItem.getId());
    return savedItem;
  }
}
