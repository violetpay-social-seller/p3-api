package io.point3.p3api.user.application.registration;

import io.point3.p3api.user.application.result.UserSyncResult;

public interface UserRegistrationUseCase {

  UserSyncResult completeRegistration(CompleteRegistrationCommand command);
}
