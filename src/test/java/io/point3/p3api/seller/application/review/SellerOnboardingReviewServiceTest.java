package io.point3.p3api.seller.application.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.SellerErrorCode;
import io.point3.p3api.seller.application.port.SellerOnboardingPersistencePort;
import io.point3.p3api.seller.application.result.SellerOnboardingReviewResult;
import io.point3.p3api.seller.domain.type.SellerOnboardingStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SellerOnboardingReviewServiceTest {

  private final SellerOnboardingPersistencePort sellerOnboardingPersistencePort =
      mock(SellerOnboardingPersistencePort.class);
  private final SellerOnboardingReviewService sellerOnboardingReviewService =
      new SellerOnboardingReviewService(sellerOnboardingPersistencePort);

  @Test
  @DisplayName("대기 중인 입점 신청을 승인한다")
  void approvesPendingOnboarding() {
    UUID onboardingId = UUID.randomUUID();
    UUID reviewerId = UUID.randomUUID();
    when(sellerOnboardingPersistencePort.existsById(onboardingId)).thenReturn(true);
    when(sellerOnboardingPersistencePort.approveIfPending(
            eq(onboardingId), eq(reviewerId), any(Instant.class)))
        .thenReturn(true);

    SellerOnboardingReviewResult result = sellerOnboardingReviewService.approve(
        ApproveSellerOnboardingCommand.from(onboardingId, reviewerId));

    assertEquals(onboardingId, result.id());
    assertEquals(SellerOnboardingStatus.APPROVED, result.status());
    assertEquals(reviewerId, result.reviewedBy());
    verify(sellerOnboardingPersistencePort)
        .approveIfPending(eq(onboardingId), eq(reviewerId), any(Instant.class));
  }

  @Test
  @DisplayName("대기 중인 입점 신청을 반려한다")
  void rejectsPendingOnboarding() {
    UUID onboardingId = UUID.randomUUID();
    UUID reviewerId = UUID.randomUUID();
    String rejectionReason = "사업자 정보가 충분하지 않습니다.";
    when(sellerOnboardingPersistencePort.existsById(onboardingId)).thenReturn(true);
    when(sellerOnboardingPersistencePort.rejectIfPending(
            eq(onboardingId), eq(reviewerId), eq(rejectionReason), any(Instant.class)))
        .thenReturn(true);

    SellerOnboardingReviewResult result = sellerOnboardingReviewService.reject(
        RejectSellerOnboardingCommand.from(onboardingId, reviewerId, rejectionReason));

    assertEquals(onboardingId, result.id());
    assertEquals(SellerOnboardingStatus.REJECTED, result.status());
    assertEquals(reviewerId, result.reviewedBy());
    verify(sellerOnboardingPersistencePort)
        .rejectIfPending(eq(onboardingId), eq(reviewerId), eq(rejectionReason), any(Instant.class));
  }

  @Test
  @DisplayName("존재하지 않는 입점 신청은 심사할 수 없다")
  void rejectsReviewForMissingOnboarding() {
    UUID onboardingId = UUID.randomUUID();
    UUID reviewerId = UUID.randomUUID();
    when(sellerOnboardingPersistencePort.existsById(onboardingId)).thenReturn(false);

    BaseException exception = assertThrows(
        BaseException.class,
        () -> sellerOnboardingReviewService.approve(
            ApproveSellerOnboardingCommand.from(onboardingId, reviewerId)));

    assertEquals(SellerErrorCode.SELLER_ONBOARDING_NOT_FOUND, exception.getErrorCode());
    verify(sellerOnboardingPersistencePort).existsById(onboardingId);
    verifyNoMoreInteractions(sellerOnboardingPersistencePort);
  }

  @Test
  @DisplayName("이미 처리된 입점 신청은 심사할 수 없다")
  void rejectsReviewForAlreadyReviewedOnboarding() {
    UUID onboardingId = UUID.randomUUID();
    UUID reviewerId = UUID.randomUUID();
    when(sellerOnboardingPersistencePort.existsById(onboardingId)).thenReturn(true);
    when(sellerOnboardingPersistencePort.approveIfPending(
            eq(onboardingId), eq(reviewerId), any(Instant.class)))
        .thenReturn(false);

    BaseException exception = assertThrows(
        BaseException.class,
        () -> sellerOnboardingReviewService.approve(
            ApproveSellerOnboardingCommand.from(onboardingId, reviewerId)));

    assertEquals(SellerErrorCode.SELLER_ONBOARDING_REVIEW_NOT_ALLOWED, exception.getErrorCode());
    verify(sellerOnboardingPersistencePort)
        .approveIfPending(eq(onboardingId), eq(reviewerId), any(Instant.class));
  }
}
