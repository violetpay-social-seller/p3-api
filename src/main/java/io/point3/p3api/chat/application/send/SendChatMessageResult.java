package io.point3.p3api.chat.application.send;

import io.point3.p3api.chat.domain.entity.ChatMessage;
import io.point3.p3api.chat.domain.entity.ChatMessageAsset;
import io.point3.p3api.chat.domain.entity.ChatTimelineItem;
import java.util.List;

public record SendChatMessageResult(
    ChatMessage chatMessage,
    ChatTimelineItem chatTimelineItem,
    List<ChatMessageAsset> chatMessageAssets) {

  public SendChatMessageResult {
    chatMessageAssets = chatMessageAssets == null ? List.of() : List.copyOf(chatMessageAssets);
  }
}
