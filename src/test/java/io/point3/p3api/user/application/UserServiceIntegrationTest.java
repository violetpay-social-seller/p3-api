package io.point3.p3api.user.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.point3.p3api.IntegrationTestSupport;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.user.application.registration.CompleteRegistrationCommand;
import io.point3.p3api.user.application.result.UserSyncResult;
import io.point3.p3api.user.application.sync.SyncCommand;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.infrastructure.persistence.UserJpaRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class UserServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private UserService userService;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Test
  @DisplayName("등록되지 않은 Cognito 사용자는 역할 선택이 필요하다고 응답한다")
  void syncsUnregisteredUser() {
    UserSyncResult result =
        userService.sync(SyncCommand.of("cognito-" + UUID.randomUUID(), uniqueEmail("new"), "신규"));

    assertFalse(result.registered());
    assertTrue(result.registrationRequired());
    assertEquals("ROLE_SELECTION", result.nextRoute());
  }

  @Test
  @DisplayName("역할 선택 완료는 사용자를 저장하고 이후 sync에서 등록 사용자로 응답한다")
  void completesRegistrationAndSyncsRegisteredUser() {
    String cognitoSub = "cognito-" + UUID.randomUUID();
    String email = uniqueEmail("buyer");

    UserSyncResult registered = userService.completeRegistration(
        CompleteRegistrationCommand.of(cognitoSub, email, "구매자", UserRole.BUYER));
    UserSyncResult synced = userService.sync(SyncCommand.of(cognitoSub, email, "구매자"));

    assertTrue(registered.registered());
    assertEquals(UserRole.BUYER, registered.role());
    assertEquals("BUYER_HOME", synced.nextRoute());
    assertEquals(registered.userId(), synced.userId());
  }

  @Test
  @DisplayName("이미 등록된 사용자가 다른 역할로 등록 완료를 재요청하면 거절한다")
  void rejectsRegistrationWithDifferentRoleForExistingUser() {
    String cognitoSub = "cognito-" + UUID.randomUUID();
    String email = uniqueEmail("seller");
    userService.completeRegistration(
        CompleteRegistrationCommand.of(cognitoSub, email, "판매자", UserRole.SELLER));

    BaseException exception = assertThrows(
        BaseException.class,
        () -> userService.completeRegistration(
            CompleteRegistrationCommand.of(cognitoSub, email, "판매자", UserRole.BUYER)));

    assertEquals(CommonErrorCode.INVALID_INPUT, exception.getErrorCode());
  }

  @Test
  @DisplayName("비활성 사용자는 sync에서 인증 실패로 처리한다")
  void rejectsInactiveUserOnSync() {
    User user = userJpaRepository.saveAndFlush(User.create(
        "cognito-" + UUID.randomUUID(), uniqueEmail("banned"), "차단 사용자", UserRole.BUYER));
    user.ban();
    userJpaRepository.flush();

    BaseException exception = assertThrows(
        BaseException.class,
        () -> userService.sync(
            SyncCommand.of(user.getCognitoSub(), user.getEmail(), user.getName())));

    assertEquals(CommonErrorCode.UNAUTHORIZED, exception.getErrorCode());
  }
}
