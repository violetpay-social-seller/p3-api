package io.point3.p3api.operator.application.command;

import io.point3.p3api.user.domain.type.UserStatus;
import java.util.UUID;

public record ChangeUserStatusCommand(
    UUID targetUserId, UUID operatorUserId, UserStatus status, String reason) {

  public static ChangeUserStatusCommand of(
      UUID targetUserId, UUID operatorUserId, UserStatus status, String reason) {
    return new ChangeUserStatusCommand(targetUserId, operatorUserId, status, reason);
  }
}
