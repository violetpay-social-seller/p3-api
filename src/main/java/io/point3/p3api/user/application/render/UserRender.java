package io.point3.p3api.user.application.render;

import io.point3.p3api.user.domain.entity.User;
import java.util.Optional;

public interface UserRender {

  Optional<User> findByCognitoSub(String cognitoSub);

  Optional<User> findActiveByCognitoSub(String cognitoSub);
}
