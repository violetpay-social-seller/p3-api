package io.point3.p3api.user.application;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.user.application.port.UserPersistencePort;
import io.point3.p3api.user.application.profile.UpdateUserProfileCommand;
import io.point3.p3api.user.application.profile.UserProfileQueryUseCase;
import io.point3.p3api.user.application.profile.UserProfileUpdateUseCase;
import io.point3.p3api.user.application.registration.CompleteRegistrationCommand;
import io.point3.p3api.user.application.registration.UserRegistrationUseCase;
import io.point3.p3api.user.application.render.UserRender;
import io.point3.p3api.user.application.result.UserProfileResult;
import io.point3.p3api.user.application.result.UserSyncResult;
import io.point3.p3api.user.application.sync.SyncCommand;
import io.point3.p3api.user.application.sync.UserSyncUseCase;
import io.point3.p3api.user.domain.entity.User;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService
    implements UserSyncUseCase,
        UserRegistrationUseCase,
        UserProfileQueryUseCase,
        UserProfileUpdateUseCase {

  private final UserPersistencePort userPersistencePort;
  private final UserRender userRender;

  @Override
  @Transactional(readOnly = true)
  public UserSyncResult sync(SyncCommand command) {
    return userRender
        .findByCognitoSub(command.cognitoSub())
        .map(user -> {
          ensureActive(user);
          return UserSyncResult.registered(user);
        })
        .orElseGet(UserSyncResult::unregistered);
  }

  @Override
  @Transactional
  public UserSyncResult completeRegistration(CompleteRegistrationCommand command) {
    // 멱등성 유지를 위한 구조
    return userRender
        .findByCognitoSub(command.cognitoSub())
        // 이미 회원이 있을 경우 막음
        .map(user -> {
          ensureActive(user);
          // 이미 Buyer인 회원이 Seller로 재요청도 막음 - 어차피 생성까지는 안가지만 UX상 응답위해
          if (user.getRole() != command.role()) {
            throw new BaseException(CommonErrorCode.INVALID_INPUT, "User role does not match");
          }
          return UserSyncResult.registered(user);
        })
        .orElseGet(() -> UserSyncResult.registered(userPersistencePort.save(
            User.create(command.cognitoSub(), command.email(), command.name(), command.role()))));
  }

  @Override
  @Transactional(readOnly = true)
  public UserProfileResult getProfile(UUID userId) {
    User user = findActiveUser(userId);

    return UserProfileResult.from(user);
  }

  @Override
  @Transactional
  public UserProfileResult updateProfile(UpdateUserProfileCommand command) {
    User user = findActiveUser(command.userId());
    validateEmailOwner(command.email(), user);
    user.updateProfile(command.email(), command.name());

    return UserProfileResult.from(user);
  }

  private User findActiveUser(UUID userId) {
    User user = userPersistencePort
        .findById(userId)
        .orElseThrow(() -> new BaseException(CommonErrorCode.UNAUTHORIZED));
    ensureActive(user);

    return user;
  }

  private void validateEmailOwner(String email, User currentUser) {
    userPersistencePort
        .findByEmail(email)
        .filter(user -> !user.getId().equals(currentUser.getId()))
        .ifPresent(user -> {
          throw new BaseException(CommonErrorCode.INVALID_INPUT, "Email already exists");
        });
  }

  private void ensureActive(User user) {
    if (!user.isActive()) {
      throw new BaseException(CommonErrorCode.UNAUTHORIZED, "User is not active");
    }
  }
}
