package io.point3.p3api.user.application;

import io.point3.p3api.user.application.port.UserPersistencePort;
import io.point3.p3api.user.application.render.UserRender;
import io.point3.p3api.user.application.sync.SyncCommand;
import io.point3.p3api.user.application.sync.UserSyncUseCase;
import io.point3.p3api.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserSyncUseCase {

    private final UserPersistencePort userPersistencePort;
    private final UserRender userRender;

    @Override
    public User findOrCreate(SyncCommand command) {
        return userRender.findByCognitoSub(command.cognitoSub())
                .orElseGet(() -> userPersistencePort.save(User.create(
                        command.cognitoSub(),
                        command.email(),
                        command.name()
                )));
    }
}
