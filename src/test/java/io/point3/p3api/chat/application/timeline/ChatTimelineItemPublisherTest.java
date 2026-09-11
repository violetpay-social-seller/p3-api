package io.point3.p3api.chat.application.timeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.point3.p3api.chat.application.port.ChatTimelineItemPort;
import io.point3.p3api.chat.application.realtime.ChatTimelineRealtimeEventPublisher;
import io.point3.p3api.chat.domain.entity.ChatTimelineItem;
import io.point3.p3api.chat.domain.type.ChatTimelineItemType;
import io.point3.p3api.inquiry.application.realtime.InquiryListChangeEventPublisher;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ChatTimelineItemPublisherTest {

  @Test
  @DisplayName("저장한 모든 타임라인 항목은 문의 목록과 열린 채팅방 갱신 이벤트를 함께 발행한다")
  void publishesInquiryAndTimelineEventsForEveryTimelineItem() {
    ChatTimelineItemPort chatTimelineItemPort = mock(ChatTimelineItemPort.class);
    InquiryListChangeEventPublisher inquiryListChangeEventPublisher =
        mock(InquiryListChangeEventPublisher.class);
    ChatTimelineRealtimeEventPublisher chatTimelineRealtimeEventPublisher =
        mock(ChatTimelineRealtimeEventPublisher.class);
    when(chatTimelineItemPort.save(any(ChatTimelineItem.class)))
        .thenAnswer(invocation -> savedItem(invocation.getArgument(0)));
    ChatTimelineItemPublisher publisher = new ChatTimelineItemPublisher(
        chatTimelineItemPort, inquiryListChangeEventPublisher, chatTimelineRealtimeEventPublisher);

    UUID inquiryId = UUID.randomUUID();
    UUID senderUserId = UUID.randomUUID();
    publisher.publishMessage(inquiryId, senderUserId, UUID.randomUUID());
    publisher.publishOrderFormSubmission(inquiryId, senderUserId, UUID.randomUUID());
    publisher.publishOrderFormRevisionRequest(inquiryId, senderUserId, UUID.randomUUID());
    publisher.publishOrderConfirmation(inquiryId, senderUserId, UUID.randomUUID());
    publisher.publishOrderConfirmationRevisionRequest(inquiryId, senderUserId, UUID.randomUUID());
    publisher.publishPaymentCompleted(inquiryId, senderUserId, UUID.randomUUID());
    publisher.publishOrderRefundRequested(inquiryId, senderUserId, UUID.randomUUID());
    publisher.publishOrderRefundCompleted(inquiryId, senderUserId, UUID.randomUUID());

    ArgumentCaptor<ChatTimelineItem> itemCaptor =
        ArgumentCaptor.forClass(ChatTimelineItem.class);
    verify(chatTimelineItemPort, times(8)).save(itemCaptor.capture());
    assertEquals(
        List.of(
            ChatTimelineItemType.MESSAGE,
            ChatTimelineItemType.ORDER_FORM_SUBMISSION,
            ChatTimelineItemType.ORDER_FORM_REVISION_REQUEST,
            ChatTimelineItemType.ORDER_CONFIRMATION,
            ChatTimelineItemType.ORDER_CONFIRMATION_REVISION,
            ChatTimelineItemType.PAYMENT_COMPLETED,
            ChatTimelineItemType.ORDER_REFUND_REQUESTED,
            ChatTimelineItemType.ORDER_REFUND_COMPLETED),
        itemCaptor.getAllValues().stream().map(ChatTimelineItem::getType).toList());
    verify(inquiryListChangeEventPublisher, times(8)).publishInquiryChanged(inquiryId);
    ArgumentCaptor<UUID> eventIdCaptor = ArgumentCaptor.forClass(UUID.class);
    verify(chatTimelineRealtimeEventPublisher, times(8))
        .publishTimelineItemCreated(eventIdCaptor.capture());
    assertIterableEquals(eventIdCaptor.getAllValues(), eventIdCaptor.getAllValues().stream()
        .distinct()
        .toList());
  }

  private ChatTimelineItem savedItem(ChatTimelineItem item) {
    ChatTimelineItem savedItem = mock(ChatTimelineItem.class);
    when(savedItem.getId()).thenReturn(UUID.randomUUID());
    when(savedItem.getInquiryId()).thenReturn(item.getInquiryId());
    when(savedItem.getType()).thenReturn(item.getType());
    return savedItem;
  }
}
