package io.point3.p3api.user.application.sync;

import io.point3.p3api.user.domain.entity.User;

public interface UserSyncUseCase {

  User findOrCreate(SyncCommand command);
}
