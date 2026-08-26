package io.point3.p3api.notification.controller;

import io.point3.p3api.auth.infrastructure.web.Authenticated;
import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.common.web.response.ApiResponse;
import io.point3.p3api.notification.application.query.NotificationQueryUseCase;
import io.point3.p3api.notification.controller.response.NotificationResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
  private final NotificationQueryUseCase notificationQueryUseCase;

  @GetMapping
  public ApiResponse<List<NotificationResponse>> getNotifications(
      @Authenticated CurrentUser currentUser) {
    return ApiResponse.ok(notificationQueryUseCase.getNotifications(currentUser.userId()).stream()
        .map(NotificationResponse::from)
        .toList());
  }

  @GetMapping("/{notificationId}")
  public ApiResponse<NotificationResponse> getNotification(
      @PathVariable UUID notificationId, @Authenticated CurrentUser currentUser) {
    return ApiResponse.ok(NotificationResponse.from(
        notificationQueryUseCase.getNotification(notificationId, currentUser.userId())));
  }

  @GetMapping("/unread-count")
  public ApiResponse<Map<String, Long>> getUnreadCount(@Authenticated CurrentUser currentUser) {
    return ApiResponse.ok(
        Map.of("unreadCount", notificationQueryUseCase.getUnreadCount(currentUser.userId())));
  }

  @PatchMapping("/{notificationId}/read")
  public ApiResponse<NotificationResponse> read(
      @PathVariable UUID notificationId, @Authenticated CurrentUser currentUser) {
    return ApiResponse.ok(NotificationResponse.from(
        notificationQueryUseCase.read(notificationId, currentUser.userId())));
  }

  @PatchMapping("/read-all")
  public ApiResponse<Void> readAll(@Authenticated CurrentUser currentUser) {
    notificationQueryUseCase.readAll(currentUser.userId());
    return ApiResponse.ok();
  }
}
