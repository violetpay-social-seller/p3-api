package io.point3.p3api.chat.application.timeline;

import io.point3.p3api.chat.application.port.ChatTimelineItemPort;
import io.point3.p3api.chat.domain.entity.ChatTimelineItem;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 채팅방에 표시할 메시지와 도메인 카드 타임라인 항목을 기록한다. */
@Component
@RequiredArgsConstructor
public class ChatTimelineItemPublisher {

  private final ChatTimelineItemPort chatTimelineItemPort;

  public ChatTimelineItem publishMessage(UUID inquiryId, UUID senderUserId, UUID chatMessageId) {
    return chatTimelineItemPort.save(
        ChatTimelineItem.message(inquiryId, senderUserId, chatMessageId));
  }

  public ChatTimelineItem publishOrderFormSubmission(
      UUID inquiryId, UUID buyerUserId, UUID submissionId) {
    return chatTimelineItemPort.save(
        ChatTimelineItem.orderFormSubmission(inquiryId, buyerUserId, submissionId));
  }

  public ChatTimelineItem publishOrderConfirmation(
      UUID inquiryId, UUID senderUserId, UUID orderConfirmationId) {
    return chatTimelineItemPort.save(
        ChatTimelineItem.orderConfirmation(inquiryId, senderUserId, orderConfirmationId));
  }

  public ChatTimelineItem publishOrderConfirmationRevisionRequest(
      UUID inquiryId, UUID buyerUserId, UUID orderConfirmationId) {
    return chatTimelineItemPort.save(ChatTimelineItem.orderConfirmationRevisionRequest(
        inquiryId, buyerUserId, orderConfirmationId));
  }
}
