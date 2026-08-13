package io.point3.p3api.user.application.sync;

import io.point3.p3api.user.application.result.UserSyncResult;

public interface UserSyncUseCase {

  UserSyncResult sync(SyncCommand command);
}
