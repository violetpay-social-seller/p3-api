package io.point3.p3api.auth.infrastructure.web;

import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.UserRole;
import java.util.UUID;

public record CurrentUser(UUID userId, String name, UserRole role) {

  public static CurrentUser from(User user) {
    return new CurrentUser(user.getId(), user.getName(), user.getRole());
  }
}
