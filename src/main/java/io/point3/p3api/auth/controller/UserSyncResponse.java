package io.point3.p3api.auth.controller;

import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.domain.type.UserStatus;
import java.util.UUID;

public record UserSyncResponse(
    UUID id, String email, String payerId, String name, UserRole role, UserStatus status) {
  public static UserSyncResponse from(User user) {
    return new UserSyncResponse(
        user.getId(),
        user.getEmail(),
        user.getPayerId(),
        user.getName(),
        user.getRole(),
        user.getStatus());
  }
}
