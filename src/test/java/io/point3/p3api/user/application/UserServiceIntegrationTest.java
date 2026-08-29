package io.point3.p3api.user.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.point3.p3api.IntegrationTestSupport;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.user.application.profile.UpdateUserProfileCommand;
import io.point3.p3api.user.application.registration.CompleteRegistrationCommand;
import io.point3.p3api.user.application.result.UserProfileResult;
import io.point3.p3api.user.application.result.UserSyncResult;
import io.point3.p3api.user.application.sync.SyncCommand;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.SignupProvider;
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

    UserSyncResult registered = userService.completeRegistration(CompleteRegistrationCommand.of(
        cognitoSub, email, "구매자", UserRole.BUYER, "010-1234-5678", SignupProvider.KAKAO));
    UserSyncResult synced = userService.sync(SyncCommand.of(cognitoSub, email, "구매자"));

    assertTrue(registered.registered());
    assertEquals(UserRole.BUYER, registered.role());
    assertEquals("BUYER_HOME", synced.nextRoute());
    assertEquals(registered.userId(), synced.userId());

    User saved = userJpaRepository.findByCognitoSub(cognitoSub).orElseThrow();
    assertEquals("010-1234-5678", saved.getPhoneNumber());
    assertEquals(SignupProvider.KAKAO, saved.getSignupProvider());
  }

  @Test
  @DisplayName("이미 등록된 사용자가 다른 역할로 등록 완료를 재요청하면 거절한다")
  void rejectsRegistrationWithDifferentRoleForExistingUser() {
    String cognitoSub = "cognito-" + UUID.randomUUID();
    String email = uniqueEmail("seller");
    userService.completeRegistration(CompleteRegistrationCommand.of(
        cognitoSub, email, "판매자", UserRole.SELLER, "010-1234-5678", SignupProvider.GOOGLE));

    BaseException exception = assertThrows(
        BaseException.class,
        () -> userService.completeRegistration(CompleteRegistrationCommand.of(
            cognitoSub, email, "판매자", UserRole.BUYER, "010-1234-5678", SignupProvider.GOOGLE)));

    assertEquals(CommonErrorCode.INVALID_INPUT, exception.getErrorCode());
  }

  @Test
  @DisplayName("비활성 사용자는 sync에서 인증 실패로 처리한다")
  void rejectsInactiveUserOnSync() {
    User user = userJpaRepository.saveAndFlush(User.create(
        "cognito-" + UUID.randomUUID(),
        uniqueEmail("banned"),
        "차단 사용자",
        UserRole.BUYER,
        "010-0000-0000",
        SignupProvider.GOOGLE));
    user.ban();
    userJpaRepository.flush();

    BaseException exception = assertThrows(
        BaseException.class,
        () -> userService.sync(
            SyncCommand.of(user.getCognitoSub(), user.getEmail(), user.getName())));

    assertEquals(CommonErrorCode.UNAUTHORIZED, exception.getErrorCode());
  }

  @Test
  @DisplayName("현재 회원은 로컬 회원 정보와 역할별 이동 정보를 조회한다")
  void getsCurrentProfile() {
    User user = userJpaRepository.saveAndFlush(User.create(
        "cognito-" + UUID.randomUUID(),
        uniqueEmail("profile"),
        "조회 사용자",
        UserRole.SELLER,
        "010-0000-0000",
        SignupProvider.GOOGLE));

    UserProfileResult result = userService.getProfile(user.getId());

    assertEquals(user.getId(), result.userId());
    assertEquals(user.getEmail(), result.email());
    assertEquals(user.getPhoneNumber(), result.phoneNumber());
    assertEquals(user.getSignupProvider(), result.signupProvider());
    assertEquals("조회 사용자", result.name());
    assertEquals(UserRole.SELLER, result.role());
    assertEquals("SELLER_HOME", result.nextRoute());
  }

  @Test
  @DisplayName("회원 정보 수정은 이메일과 이름을 갱신한다")
  void updatesProfile() {
    User user = userJpaRepository.saveAndFlush(User.create(
        "cognito-" + UUID.randomUUID(),
        uniqueEmail("profile-update"),
        "기존 이름",
        UserRole.BUYER,
        "010-0000-0000",
        SignupProvider.GOOGLE));
    String updatedEmail = uniqueEmail("profile-updated");

    UserProfileResult result =
        userService.updateProfile(UpdateUserProfileCommand.of(user.getId(), updatedEmail, "변경 이름"));

    assertEquals(user.getId(), result.userId());
    assertEquals(updatedEmail, result.email());
    assertEquals("변경 이름", result.name());
    assertEquals("BUYER_HOME", result.nextRoute());
  }

  @Test
  @DisplayName("회원 정보 수정은 다른 회원의 이메일로 변경할 수 없다")
  void rejectsDuplicateEmailOnUpdate() {
    User user = userJpaRepository.saveAndFlush(User.create(
        "cognito-" + UUID.randomUUID(),
        uniqueEmail("profile-me"),
        "수정 사용자",
        UserRole.BUYER,
        "010-0000-0000",
        SignupProvider.GOOGLE));
    User other = userJpaRepository.saveAndFlush(User.create(
        "cognito-" + UUID.randomUUID(),
        uniqueEmail("profile-other"),
        "다른 사용자",
        UserRole.SELLER,
        "010-0000-0000",
        SignupProvider.GOOGLE));

    BaseException exception = assertThrows(
        BaseException.class,
        () -> userService.updateProfile(
            UpdateUserProfileCommand.of(user.getId(), other.getEmail().toUpperCase(), "변경 이름")));

    assertEquals(CommonErrorCode.INVALID_INPUT, exception.getErrorCode());
  }
}
