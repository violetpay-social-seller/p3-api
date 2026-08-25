package io.point3.p3api.user.application.profile;

import io.point3.p3api.user.application.result.UserProfileResult;

public interface UserProfileUpdateUseCase {

  UserProfileResult updateProfile(UpdateUserProfileCommand command);
}
