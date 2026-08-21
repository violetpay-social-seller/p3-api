package io.point3.p3api.seller.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.SellerErrorCode;
import io.point3.p3api.seller.application.port.SellerOnboardingPersistencePort;
import io.point3.p3api.seller.application.reapply.ReapplySellerOnboardingCommand;
import io.point3.p3api.seller.application.result.SellerOnboardingDetailResult;
import io.point3.p3api.seller.application.result.SellerOnboardingResult;
import io.point3.p3api.seller.domain.entity.SellerOnboarding;
import io.point3.p3api.seller.domain.type.SellerOnboardingStatus;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SellerOnboardingStatusServiceTest {

  private final SellerOnboardingPersistencePort sellerOnboardingPersistencePort = mock(
      SellerOnboardingPersistencePort.class);
  private final SellerOnboardingService sellerOnboardingService = new SellerOnboardingService(
      sellerOnboardingPersistencePort);

  @Test
  @DisplayName("최신 신청 상태를 조회한다")
  void getsCurrentOnboarding() {
    UUID applicantUserId = UUID.randomUUID();
    UUID onboardingId = UUID.randomUUID();
    SellerOnboarding onboarding = onboarding(onboardingId, SellerOnboardingStatus.REJECTED);
    when(sellerOnboardingPersistencePort.findLatestByApplicantUserId(applicantUserId)).thenReturn(
        Optional.of(onboarding));

    SellerOnboardingDetailResult result = sellerOnboardingService.getCurrentOnboarding(
        applicantUserId);

    assertEquals(onboardingId, result.id());
    assertEquals(SellerOnboardingStatus.REJECTED, result.status());
    assertEquals("사업자 정보가 충분하지 않습니다.", result.rejectionReason());
  }

  @Test
  @DisplayName("반려된 최신 신청은 새 PENDING 신청으로 재신청할 수 있다")
  void reappliesRejectedLatestOnboarding() {
    UUID applicantUserId = UUID.randomUUID();
    UUID onboardingId = UUID.randomUUID();
    SellerOnboarding onboarding = onboarding(onboardingId, SellerOnboardingStatus.REJECTED);
    when(sellerOnboardingPersistencePort.findByIdAndApplicantUserId(onboardingId, applicantUserId))
        .thenReturn(Optional.of(onboarding));
    when(sellerOnboardingPersistencePort.findLatestByApplicantUserId(applicantUserId)).thenReturn(
        Optional.of(onboarding));
    when(sellerOnboardingPersistencePort.save(any(SellerOnboarding.class))).thenAnswer(
        invocation -> invocation.getArgument(0));

    SellerOnboardingResult result = sellerOnboardingService.reapply(command(onboardingId, applicantUserId));

    assertEquals(applicantUserId, result.applicantUserId());
    assertEquals(SellerOnboardingStatus.PENDING, result.status());
  }

  @Test
  @DisplayName("최신 신청이 반려 상태가 아니면 재신청할 수 없다")
  void rejectsReapplicationForNonRejectedLatestOnboarding() {
    UUID applicantUserId = UUID.randomUUID();
    UUID onboardingId = UUID.randomUUID();
    SellerOnboarding onboarding = onboarding(onboardingId, SellerOnboardingStatus.PENDING);
    when(sellerOnboardingPersistencePort.findByIdAndApplicantUserId(onboardingId, applicantUserId))
        .thenReturn(Optional.of(onboarding));
    when(sellerOnboardingPersistencePort.findLatestByApplicantUserId(applicantUserId)).thenReturn(
        Optional.of(onboarding));

    BaseException exception = assertThrows(
        BaseException.class, () -> sellerOnboardingService.reapply(command(onboardingId, applicantUserId)));

    assertEquals(SellerErrorCode.SELLER_ONBOARDING_REAPPLICATION_NOT_ALLOWED, exception.getErrorCode());
  }

  private SellerOnboarding onboarding(UUID onboardingId, SellerOnboardingStatus status) {
    SellerOnboarding onboarding = mock(SellerOnboarding.class);
    when(onboarding.getId()).thenReturn(onboardingId);
    when(onboarding.getStatus()).thenReturn(status);
    when(onboarding.getRejectionReason()).thenReturn("사업자 정보가 충분하지 않습니다.");
    return onboarding;
  }

  private ReapplySellerOnboardingCommand command(UUID onboardingId, UUID applicantUserId) {
    return ReapplySellerOnboardingCommand.from(
        onboardingId,
        applicantUserId,
        "P3 베이커리",
        "010-1234-5678",
        "서울특별시 중구",
        null);
  }
}
