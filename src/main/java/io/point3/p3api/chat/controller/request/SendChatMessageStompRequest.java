package io.point3.p3api.chat.controller.request;

import java.util.List;
import java.util.UUID;

/** 클라이언트가 STOMP 송신 목적지로 전달하는 채팅 메시지 요청이다. */
public record SendChatMessageStompRequest(String content, List<UUID> assetIds) {

  public SendChatMessageStompRequest {
    assetIds = assetIds == null ? List.of() : List.copyOf(assetIds);
  }
}
