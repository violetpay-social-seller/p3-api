package io.point3.p3api.notification.infrastructure.sse;

import io.point3.p3api.notification.application.result.NotificationResult;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** 사용자별 SSE 연결을 보관하고 저장된 알림을 전달한다. */
@Slf4j
@Component
public class NotificationSseConnectionRegistry {
  private static final long TIMEOUT_MILLIS = 30 * 60 * 1000L;

  private final Map<UUID, Map<UUID, SseEmitter>> emitters = new ConcurrentHashMap<>();

  public SseEmitter connect(UUID userId) {
    UUID connectionId = UUID.randomUUID();
    SseEmitter emitter = new SseEmitter(TIMEOUT_MILLIS);
    emitters
        .computeIfAbsent(userId, ignored -> new ConcurrentHashMap<>())
        .put(connectionId, emitter);
    emitter.onCompletion(() -> remove(userId, connectionId));
    emitter.onTimeout(() -> remove(userId, connectionId));
    emitter.onError(error -> remove(userId, connectionId));

    sendConnected(userId, connectionId, emitter);
    return emitter;
  }

  public void send(UUID userId, NotificationResult notification) {
    Map<UUID, SseEmitter> userEmitters = emitters.get(userId);
    if (userEmitters == null) {
      return;
    }

    userEmitters.forEach(
        (connectionId, emitter) -> sendNotification(userId, connectionId, emitter, notification));
  }

  private void sendConnected(UUID userId, UUID connectionId, SseEmitter emitter) {
    try {
      emitter.send(SseEmitter.event().name("connected").reconnectTime(3000));
    } catch (IOException | IllegalStateException e) {
      log.debug("Failed to establish notification SSE connection: userId={}", userId, e);
      remove(userId, connectionId);
    }
  }

  private void sendNotification(
      UUID userId, UUID connectionId, SseEmitter emitter, NotificationResult notification) {
    try {
      emitter.send(SseEmitter.event()
          .id(notification.id().toString())
          .name("notification.created")
          .data(notification));
    } catch (IOException | IllegalStateException e) {
      log.debug("Failed to send notification SSE event: userId={}", userId, e);
      remove(userId, connectionId);
    }
  }

  private void remove(UUID userId, UUID connectionId) {
    Map<UUID, SseEmitter> userEmitters = emitters.get(userId);
    if (userEmitters == null) {
      return;
    }

    userEmitters.remove(connectionId);
    if (userEmitters.isEmpty()) {
      emitters.remove(userId, userEmitters);
    }
  }
}
