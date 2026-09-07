package io.point3.p3api.inquiry.controller;

import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** 문의 목록 갱신 STOMP 구독 목적지를 제공한다. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InquiryListStompDestination {

  private static final String TOPIC_USER_PREFIX = "/topic/users/";
  private static final String INQUIRIES_PATH = "/inquiries";

  public static String topicDestination(UUID userId) {
    return TOPIC_USER_PREFIX + userId + INQUIRIES_PATH;
  }

  public static Optional<UUID> topicUserId(String destination) {
    if (destination == null
        || !destination.startsWith(TOPIC_USER_PREFIX)
        || !destination.endsWith(INQUIRIES_PATH)) {
      return Optional.empty();
    }

    String userId = destination.substring(
        TOPIC_USER_PREFIX.length(), destination.length() - INQUIRIES_PATH.length());
    if (userId.isBlank() || userId.contains("/")) {
      return Optional.empty();
    }

    try {
      return Optional.of(UUID.fromString(userId));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }
}
