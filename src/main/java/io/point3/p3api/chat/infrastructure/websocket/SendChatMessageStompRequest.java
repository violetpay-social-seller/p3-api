package io.point3.p3api.chat.infrastructure.websocket;

import jakarta.validation.constraints.NotBlank;

/** 클라이언트가 STOMP 송신 목적지로 전달하는 채팅 메시지 요청 */
public record SendChatMessageStompRequest(@NotBlank String content) {}
