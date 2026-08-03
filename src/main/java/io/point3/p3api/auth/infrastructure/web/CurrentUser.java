package io.point3.p3api.auth.infrastructure.web;

import io.point3.p3api.user.domain.entity.User;

import java.util.UUID;

public record CurrentUser(UUID userId, String name) {

    public static CurrentUser from(User user) {
        return new CurrentUser(user.getId(), user.getName());
    }
}
