package io.point3.p3api.user.application.port;

import io.point3.p3api.user.domain.entity.User;
import java.util.Optional;
import java.util.UUID;

public interface UserPersistencePort {

  User save(User user);

  Optional<User> findById(UUID userId);
}
