package io.point3.p3api.user.infrastructure;

import io.point3.p3api.user.application.render.UserRender;
import io.point3.p3api.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRender {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<User> findByCognitoSub(String cognitoSub) {
        return userJpaRepository.findByCognitoSub(cognitoSub);
    }
}
c