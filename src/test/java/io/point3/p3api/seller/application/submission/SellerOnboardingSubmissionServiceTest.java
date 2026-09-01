package io.point3.p3api.seller.application.submission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.seller.application.create.CreateSellerOnboardingCommand;
import io.point3.p3api.seller.application.create.SellerOnboardingCreateUseCase;
import io.point3.p3api.seller.application.result.SellerOnboardingResult;
import io.point3.p3api.user.application.registration.CompleteRegistrationCommand;
import io.point3.p3api.user.application.registration.UserRegistrationUseCase;
import io.point3.p3api.user.application.result.UserSyncResult;
import io.point3.p3api.user.domain.type.SignupProvider;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.domain.type.UserStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SellerOnboardingSubmissionServiceTest {

  private final UserRegistrationUseCase userRegistrationUseCase =
      mock(UserRegistrationUseCase.class);
  private final SellerOnboardingCreateUseCase sellerOnboardingCreateUseCase =
      mock(SellerOnboardingCreateUseCase.class);
  private final SellerOnboardingSubmissionService sellerOnboardingSubmissionService =
      new SellerOnboardingSubmissionService(userRegistrationUseCase, sellerOnboardingCreateUseCase);

  @Test
  @DisplayName("SELLER 등록 후 해당 사용자로 입점 신청을 생성한다")
  void submitsOnboardingAfterSellerRegistration() {
    UUID userId = UUID.randomUUID();
    when(userRegistrationUseCase.completeRegistration(any())).thenReturn(registeredSeller(userId));
    when(sellerOnboardingCreateUseCase.create(any())).thenReturn(onboardingResult(userId));

    sellerOnboardingSubmissionService.submit(command());

    ArgumentCaptor<CreateSellerOnboardingCommand> captor =
        ArgumentCaptor.forClass(CreateSellerOnboardingCommand.class);
    verify(sellerOnboardingCreateUseCase).create(captor.capture());
    assertEquals(userId, captor.getValue().applicantUserId());
    assertEquals("P3 베이커리", captor.getValue().storeName());
  }

  @Test
  @DisplayName("SELLER가 아닌 등록 결과로는 입점 신청을 생성하지 않는다")
  void rejectsNonSellerRegistration() {
    when(userRegistrationUseCase.completeRegistration(any()))
        .thenReturn(new UserSyncResult(
            true,
            false,
            UUID.randomUUID(),
            "buyer@example.com",
            "구매자",
            UserRole.BUYER,
            UserStatus.ACTIVE,
            "BUYER_HOME"));

    assertThrows(BaseException.class, () -> sellerOnboardingSubmissionService.submit(command()));

    verifyNoInteractions(sellerOnboardingCreateUseCase);
  }

  private SubmitSellerOnboardingCommand command() {
    return SubmitSellerOnboardingCommand.of(
        CompleteRegistrationCommand.of(
            "cognito-sub",
            "seller@example.com",
            "카카오 닉네임",
            UserRole.SELLER,
            "010-1234-5678",
            SignupProvider.KAKAO),
        "P3 베이커리",
        "010-1234-5678",
        "서울특별시 중구",
        null);
  }

  private UserSyncResult registeredSeller(UUID userId) {
    return new UserSyncResult(
        true,
        false,
        userId,
        "seller@example.com",
        "카카오 닉네임",
        UserRole.SELLER,
        UserStatus.ACTIVE,
        "SELLER_HOME");
  }

  private SellerOnboardingResult onboardingResult(UUID userId) {
    return new SellerOnboardingResult(
        UUID.randomUUID(),
        userId,
        "P3 베이커리",
        "010-1234-5678",
        "서울특별시 중구",
        null,
        io.point3.p3api.seller.domain.type.SellerOnboardingStatus.PENDING,
        Instant.now());
  }
}
