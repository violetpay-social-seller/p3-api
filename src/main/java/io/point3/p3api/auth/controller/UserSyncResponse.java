package io.point3.p3api.auth.controller;

import io.point3.p3api.user.application.result.UserSyncResult;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.domain.type.UserStatus;

public record UserSyncResponse(
    boolean registered,
    boolean registrationRequired,
    UserRole role,
    UserStatus status,
    String nextRoute) {
  public static UserSyncResponse from(UserSyncResult result) {
    return new UserSyncResponse(
        result.registered(),
        result.registrationRequired(),
        result.role(),
        result.status(),
        result.nextRoute());
  }
}
