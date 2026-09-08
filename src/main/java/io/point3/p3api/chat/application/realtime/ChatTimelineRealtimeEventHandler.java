package io.point3.p3api.chat.application.realtime;

import io.point3.p3api.chat.application.port.ChatMessageAssetPort;
import io.point3.p3api.chat.application.port.ChatMessagePort;
import io.point3.p3api.chat.application.port.ChatTimelineItemPort;
import io.point3.p3api.chat.application.port.ChatTimelineRealtimePublisherPort;
import io.point3.p3api.chat.application.timeline.result.ChatTimelineItemResult;
import io.point3.p3api.chat.domain.entity.ChatMessage;
import io.point3.p3api.chat.domain.entity.ChatMessageAsset;
import io.point3.p3api.chat.domain.entity.ChatTimelineItem;
import io.point3.p3api.chat.domain.type.ChatTimelineItemType;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.ChatErrorCode;
import io.point3.p3api.exception.code.CommonErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** 커밋된 채팅 타임라인 항목을 열린 문의방 STOMP topic으로 발행 */
@Component
@RequiredArgsConstructor
public class ChatTimelineRealtimeEventHandler {

  private final ChatTimelineItemPort chatTimelineItemPort;
  private final ChatMessagePort chatMessagePort;
  private final ChatMessageAssetPort chatMessageAssetPort;
  private final ChatTimelineRealtimePublisherPort chatTimelineRealtimePublisherPort;

  @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void publish(ChatTimelineItemCreatedEvent event) {
    ChatTimelineItem item = chatTimelineItemPort
        .findById(event.eventId())
        .orElseThrow(() -> new BaseException(ChatErrorCode.CHAT_INQUIRY_NOT_FOUND));

    ChatTimelineItemResult result = item.getType() == ChatTimelineItemType.MESSAGE
        ? messageResult(item)
        : ChatTimelineItemResult.from(item, null);
    chatTimelineRealtimePublisherPort.publish(new ChatTimelineRealtimeEvent(
        item.getInquiryId(), ChatTimelineRealtimePayload.from(result)));
  }

  private ChatTimelineItemResult messageResult(ChatTimelineItem item) {
    ChatMessage message = chatMessagePort.findAllById(List.of(item.getReferenceId())).stream()
        .findFirst()
        .orElseThrow(() -> new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR));
    List<ChatMessageAsset> assets =
        chatMessageAssetPort.findAllByMessageIdIn(List.of(message.getId()));
    return ChatTimelineItemResult.from(item, message, assets);
  }
}
