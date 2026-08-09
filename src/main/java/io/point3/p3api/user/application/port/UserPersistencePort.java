package io.point3.p3api.user.application.port;

import io.point3.p3api.user.domain.entity.User;


public interface UserPersistencePort {

    User save(User user);

}
