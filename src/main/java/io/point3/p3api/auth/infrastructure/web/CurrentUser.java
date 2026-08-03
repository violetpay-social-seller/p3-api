package io.point3.p3api.auth.infrastructure.web;

import java.util.UUID;

public record CurrentUser(UUID userId, String name) { }
