package io.point3.p3api.user.application.profile;

import java.util.Objects;
import java.util.UUID;

public record UpdateUserProfileCommand(UUID userId, String email, String name) {

  public UpdateUserProfileCommand {
    Objects.requireNonNull(userId, "userId");
    Objects.requireNonNull(email, "email");
    Objects.requireNonNull(name, "name");
  }

  public static UpdateUserProfileCommand of(UUID userId, String email, String name) {
    return new UpdateUserProfileCommand(userId, email, name);
  }
}
