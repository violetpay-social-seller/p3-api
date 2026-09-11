package io.point3.p3api.chat.application.realtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.point3.p3api.chat.application.port.ChatMessageAssetPort;
import io.point3.p3api.chat.application.port.ChatMessagePort;
import io.point3.p3api.chat.application.port.ChatTimelineItemPort;
import io.point3.p3api.chat.application.port.ChatTimelineRealtimePublisherPort;
import io.point3.p3api.chat.domain.entity.ChatMessage;
import io.point3.p3api.chat.domain.entity.ChatMessageAsset;
import io.point3.p3api.chat.domain.entity.ChatTimelineItem;
import io.point3.p3api.chat.domain.type.ChatTimelineItemType;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

class ChatTimelineRealtimeEventHandlerTest {

  @ParameterizedTest
  @EnumSource(
      value = ChatTimelineItemType.class,
      names = {
        "ORDER_FORM_SUBMISSION",
        "ORDER_FORM_REVISION_REQUEST",
        "ORDER_CONFIRMATION",
        "ORDER_CONFIRMATION_REVISION",
        "PAYMENT_COMPLETED",
        "ORDER_REFUND_REQUESTED",
        "ORDER_REFUND_COMPLETED"
      })
  @DisplayName("CTA 타임라인 항목을 열린 채팅방 topic payload로 발행한다")
  void publishesCtaTimelineItem(ChatTimelineItemType type) {
    Fixture fixture = fixture();
    ChatTimelineItem item = timelineItem(type);
    when(fixture.chatTimelineItemPort.findById(item.getId())).thenReturn(Optional.of(item));

    fixture.handler.publish(new ChatTimelineItemCreatedEvent(item.getId()));

    ChatTimelineRealtimeEvent event = publishedEvent(fixture.chatTimelineRealtimePublisherPort);
    assertEquals(item.getInquiryId(), event.inquiryId());
    assertEquals(item.getId(), event.payload().eventId());
    assertEquals(item.getReferenceId(), event.payload().referenceId());
    assertEquals(item.getType(), event.payload().type());
    assertEquals(item.getSenderUserId(), event.payload().senderUserId());
    assertEquals(item.getCreatedAt(), event.payload().createdAt());
    assertNull(event.payload().content());
    assertEquals(List.of(), event.payload().assetIds());
    verify(fixture.chatMessagePort, never()).findAllById(any());
    verify(fixture.chatMessageAssetPort, never()).findAllByMessageIdIn(any());
  }

  @Test
  @DisplayName("MESSAGE 타임라인 항목은 메시지 본문과 첨부 assetIds를 함께 발행한다")
  void publishesMessageTimelineItemWithContentAndAssetIds() {
    Fixture fixture = fixture();
    ChatTimelineItem item = timelineItem(ChatTimelineItemType.MESSAGE);
    ChatMessage message = chatMessage(item.getReferenceId());
    UUID assetId = UUID.randomUUID();
    ChatMessageAsset asset = mock(ChatMessageAsset.class);
    when(asset.getAssetId()).thenReturn(assetId);
    when(fixture.chatTimelineItemPort.findById(item.getId())).thenReturn(Optional.of(item));
    when(fixture.chatMessagePort.findAllById(List.of(item.getReferenceId())))
        .thenReturn(List.of(message));
    when(fixture.chatMessageAssetPort.findAllByMessageIdIn(List.of(message.getId())))
        .thenReturn(List.of(asset));

    fixture.handler.publish(new ChatTimelineItemCreatedEvent(item.getId()));

    ChatTimelineRealtimePayload payload =
        publishedEvent(fixture.chatTimelineRealtimePublisherPort).payload();
    assertEquals(item.getId(), payload.eventId());
    assertEquals(item.getReferenceId(), payload.referenceId());
    assertEquals(ChatTimelineItemType.MESSAGE, payload.type());
    assertEquals("안녕하세요", payload.content());
    assertEquals(List.of(assetId), payload.assetIds());
  }

  @Test
  @DisplayName("타임라인 실시간 발행은 트랜잭션 커밋 이후에 실행한다")
  void listensAfterTransactionCommit() throws NoSuchMethodException {
    Method method = ChatTimelineRealtimeEventHandler.class.getDeclaredMethod(
        "publish", ChatTimelineItemCreatedEvent.class);

    TransactionalEventListener listener =
        method.getAnnotation(TransactionalEventListener.class);

    assertNotNull(listener);
    assertEquals(TransactionPhase.AFTER_COMMIT, listener.phase());
  }

  private Fixture fixture() {
    ChatTimelineItemPort chatTimelineItemPort = mock(ChatTimelineItemPort.class);
    ChatMessagePort chatMessagePort = mock(ChatMessagePort.class);
    ChatMessageAssetPort chatMessageAssetPort = mock(ChatMessageAssetPort.class);
    ChatTimelineRealtimePublisherPort chatTimelineRealtimePublisherPort =
        mock(ChatTimelineRealtimePublisherPort.class);
    return new Fixture(
        chatTimelineItemPort,
        chatMessagePort,
        chatMessageAssetPort,
        chatTimelineRealtimePublisherPort,
        new ChatTimelineRealtimeEventHandler(
            chatTimelineItemPort,
            chatMessagePort,
            chatMessageAssetPort,
            chatTimelineRealtimePublisherPort));
  }

  private ChatTimelineItem timelineItem(ChatTimelineItemType type) {
    ChatTimelineItem item = mock(ChatTimelineItem.class);
    when(item.getId()).thenReturn(UUID.randomUUID());
    when(item.getInquiryId()).thenReturn(UUID.randomUUID());
    when(item.getReferenceId()).thenReturn(UUID.randomUUID());
    when(item.getType()).thenReturn(type);
    when(item.getSenderUserId()).thenReturn(UUID.randomUUID());
    when(item.getCreatedAt()).thenReturn(Instant.parse("2026-08-17T10:00:00Z"));
    return item;
  }

  private ChatMessage chatMessage(UUID messageId) {
    ChatMessage message = mock(ChatMessage.class);
    when(message.getId()).thenReturn(messageId);
    when(message.getContent()).thenReturn("안녕하세요");
    return message;
  }

  private ChatTimelineRealtimeEvent publishedEvent(
      ChatTimelineRealtimePublisherPort publisherPort) {
    ArgumentCaptor<ChatTimelineRealtimeEvent> eventCaptor =
        ArgumentCaptor.forClass(ChatTimelineRealtimeEvent.class);
    verify(publisherPort).publish(eventCaptor.capture());
    return eventCaptor.getValue();
  }

  private record Fixture(
      ChatTimelineItemPort chatTimelineItemPort,
      ChatMessagePort chatMessagePort,
      ChatMessageAssetPort chatMessageAssetPort,
      ChatTimelineRealtimePublisherPort chatTimelineRealtimePublisherPort,
      ChatTimelineRealtimeEventHandler handler) {}
}
