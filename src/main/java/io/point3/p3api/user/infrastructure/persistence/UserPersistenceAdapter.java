package io.point3.p3api.user.infrastructure.persistence;

import io.point3.p3api.user.application.port.UserPersistencePort;
import io.point3.p3api.user.application.render.UserRender;
import io.point3.p3api.user.domain.entity.User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRender, UserPersistencePort {

  private final UserJpaRepository userJpaRepository;

  @Override
  public Optional<User> findByCognitoSub(String cognitoSub) {
    return userJpaRepository.findByCognitoSub(cognitoSub);
  }

  @Override
  public Optional<User> findActiveByCognitoSub(String cognitoSub) {
    return userJpaRepository.findActiveByCognitoSub(cognitoSub);
  }

  @Override
  public User save(User user) {
    return userJpaRepository.save(user);
  }
}
