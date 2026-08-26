package io.point3.p3api.operator.application.query;

import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.domain.type.UserStatus;

public record OperatorUserQuery(
    String keyword, UserRole role, UserStatus status, OperatorPageQuery pageQuery) {}
