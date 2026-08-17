package io.point3.p3api.chat.infrastructure.websocket;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** STOMP 채팅 메시지 송신 및 구독 목적지를 제공 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChatStompDestination {

  public static final String MESSAGE_MAPPING = "/inquiries/{inquiryId}/messages";

  private static final String INQUIRIES_PATH = "/inquiries/";
  private static final String MESSAGES_PATH = "/messages";

  public static String messageMapping(UUID inquiryId) {
    return INQUIRIES_PATH + inquiryId + MESSAGES_PATH;
  }

  public static String sendDestination(UUID inquiryId) {
    return "/app" + messageMapping(inquiryId);
  }

  public static String topicDestination(UUID inquiryId) {
    return "/topic" + INQUIRIES_PATH + inquiryId;
  }
}
