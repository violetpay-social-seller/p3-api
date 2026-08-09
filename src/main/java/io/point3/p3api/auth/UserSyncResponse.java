package io.point3.p3api.auth;

import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.UserStatus;

import java.util.UUID;

public record UserSyncResponse(
        UUID id, String email, String name, UserStatus status
) {
    public static UserSyncResponse from(User user) {
        return new UserSyncResponse(user.getId(), user.getEmail(), user.getName(), user.getStatus());
    }

}
