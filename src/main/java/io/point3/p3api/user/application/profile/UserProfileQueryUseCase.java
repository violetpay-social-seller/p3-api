package io.point3.p3api.user.application.profile;

import io.point3.p3api.user.application.result.UserProfileResult;
import java.util.UUID;

public interface UserProfileQueryUseCase {

  UserProfileResult getProfile(UUID userId);
}
