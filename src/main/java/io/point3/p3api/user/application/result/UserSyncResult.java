package io.point3.p3api.user.application.result;

import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.domain.type.UserStatus;
import java.util.UUID;

public record UserSyncResult(
    boolean registered,
    boolean registrationRequired,
    UUID userId,
    String email,
    String name,
    UserRole role,
    UserStatus status,
    String nextRoute) {
  public static UserSyncResult registered(User user) {
    return new UserSyncResult(
        true,
        false,
        user.getId(),
        user.getEmail(),
        user.getName(),
        user.getRole(),
        user.getStatus(),
        nextRoute(user.getRole()));
  }

  public static UserSyncResult unregistered() {
    return new UserSyncResult(false, true, null, null, null, null, null, "ROLE_SELECTION");
  }

  private static String nextRoute(UserRole role) {
    return switch (role) {
      case BUYER -> "BUYER_HOME";
      case SELLER -> "SELLER_HOME";
      case OPERATOR -> "ADMIN_HOME";
    };
  }
}
