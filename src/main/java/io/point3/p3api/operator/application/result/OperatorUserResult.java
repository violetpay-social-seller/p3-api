package io.point3.p3api.operator.application.result;

import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.domain.type.UserStatus;
import java.time.Instant;
import java.util.UUID;

public record OperatorUserResult(
    UUID id,
    String email,
    String payerId,
    String name,
    UserRole role,
    UserStatus status,
    Instant createdAt,
    Instant updatedAt) {

  public static OperatorUserResult from(User user) {
    return new OperatorUserResult(
        user.getId(),
        user.getEmail(),
        user.getPayerId(),
        user.getName(),
        user.getRole(),
        user.getStatus(),
        user.getCreatedAt(),
        user.getUpdatedAt());
  }
}
