package io.point3.p3api.seller.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.SellerErrorCode;
import io.point3.p3api.seller.application.create.CreateSellerOnboardingCommand;
import io.point3.p3api.seller.application.port.SellerOnboardingPersistencePort;
import io.point3.p3api.seller.domain.entity.SellerOnboarding;
import io.point3.p3api.seller.domain.type.SellerOnboardingStatus;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SellerOnboardingServiceTest {

  private final SellerOnboardingPersistencePort sellerOnboardingPersistencePort = mock(
      SellerOnboardingPersistencePort.class);
  private final SellerOnboardingService sellerOnboardingService = new SellerOnboardingService(
      sellerOnboardingPersistencePort);

  @Test
  @DisplayName("대기 중인 신청이 없으면 PENDING 입점 신청을 저장한다")
  void createsOnboardingWhenNoPendingOnboardingExists() {
    UUID applicantUserId = UUID.randomUUID();
    CreateSellerOnboardingCommand command = command(applicantUserId);
    when(sellerOnboardingPersistencePort.save(any(SellerOnboarding.class))).thenAnswer(
        invocation -> invocation.getArgument(0));

    sellerOnboardingService.create(command);

    ArgumentCaptor<SellerOnboarding> captor = ArgumentCaptor.forClass(SellerOnboarding.class);
    verify(sellerOnboardingPersistencePort).save(captor.capture());
    assertEquals(applicantUserId, captor.getValue().getApplicantUserId());
    assertEquals(SellerOnboardingStatus.PENDING, captor.getValue().getStatus());
  }

  @Test
  @DisplayName("대기 중인 신청이 있으면 중복 신청을 거절한다")
  void rejectsOnboardingWhenPendingOnboardingExists() {
    UUID applicantUserId = UUID.randomUUID();
    when(sellerOnboardingPersistencePort.existsPendingByApplicantUserId(applicantUserId))
        .thenReturn(true);

    BaseException exception = assertThrows(
        BaseException.class, () -> sellerOnboardingService.create(command(applicantUserId)));

    assertEquals(SellerErrorCode.SELLER_ONBOARDING_PENDING_ALREADY_EXISTS, exception.getErrorCode());
    verify(sellerOnboardingPersistencePort).existsPendingByApplicantUserId(applicantUserId);
    verifyNoMoreInteractions(sellerOnboardingPersistencePort);
  }

  private CreateSellerOnboardingCommand command(UUID applicantUserId) {
    return CreateSellerOnboardingCommand.from(
        applicantUserId,
        "P3 베이커리",
        "010-1234-5678",
        "서울특별시 중구",
        null);
  }
}
