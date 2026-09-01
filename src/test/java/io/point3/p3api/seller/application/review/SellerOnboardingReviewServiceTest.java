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
import io.point3.p3api.seller.domain.entity.SellerOnboarding;
import io.point3.p3api.seller.domain.type.SellerOnboardingStatus;
import io.point3.p3api.store.application.create.CreateStoreCommand;
import io.point3.p3api.store.application.create.StoreCreateUseCase;
import io.point3.p3api.store.application.port.StorePersistencePort;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SellerOnboardingReviewServiceTest {

  private final SellerOnboardingPersistencePort sellerOnboardingPersistencePort =
      mock(SellerOnboardingPersistencePort.class);
  private final StoreCreateUseCase storeCreateUseCase = mock(StoreCreateUseCase.class);
  private final StorePersistencePort storePersistencePort = mock(StorePersistencePort.class);
  private final SellerOnboardingReviewService sellerOnboardingReviewService =
      new SellerOnboardingReviewService(
          sellerOnboardingPersistencePort, storeCreateUseCase, storePersistencePort);

  @Test
  @DisplayName("대기 중인 입점 신청을 승인한다")
  void approvesPendingOnboarding() {
    UUID onboardingId = UUID.randomUUID();
    UUID reviewerId = UUID.randomUUID();
    SellerOnboarding onboarding = onboarding(onboardingId);
    when(sellerOnboardingPersistencePort.findById(onboardingId))
        .thenReturn(Optional.of(onboarding));
    when(sellerOnboardingPersistencePort.approveIfPending(
            eq(onboardingId), eq(reviewerId), any(Instant.class)))
        .thenReturn(true);
    when(storePersistencePort.existsByOwnerUserId(onboarding.getApplicantUserId()))
        .thenReturn(false);

    SellerOnboardingReviewResult result = sellerOnboardingReviewService.approve(
        ApproveSellerOnboardingCommand.from(onboardingId, reviewerId));

    assertEquals(onboardingId, result.id());
    assertEquals(SellerOnboardingStatus.APPROVED, result.status());
    assertEquals(reviewerId, result.reviewedBy());
    verify(sellerOnboardingPersistencePort)
        .approveIfPending(eq(onboardingId), eq(reviewerId), any(Instant.class));
    ArgumentCaptor<CreateStoreCommand> captor = ArgumentCaptor.forClass(CreateStoreCommand.class);
    verify(storeCreateUseCase).create(captor.capture());
    CreateStoreCommand command = captor.getValue();
    assertEquals(onboarding.getApplicantUserId(), command.ownerUserId());
    assertEquals(onboarding.getStoreName(), command.name());
    assertEquals(onboarding.getPhoneNumber(), command.contact());
    assertEquals("\"https://instagram.com/p3\"", command.snsLinks());
    assertEquals(onboarding.getAddress(), command.address());
    Assertions.assertFalse(command.contactVisible());
  }

  @Test
  @DisplayName("대기 중인 입점 신청을 반려한다")
  void rejectsPendingOnboarding() {
    UUID onboardingId = UUID.randomUUID();
    UUID reviewerId = UUID.randomUUID();
    String rejectionReason = "사업자 정보가 충분하지 않습니다.";
    SellerOnboarding onboarding = onboarding(onboardingId);
    when(sellerOnboardingPersistencePort.findById(onboardingId))
        .thenReturn(Optional.of(onboarding));
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
    when(sellerOnboardingPersistencePort.findById(onboardingId)).thenReturn(Optional.empty());

    BaseException exception = assertThrows(
        BaseException.class,
        () -> sellerOnboardingReviewService.approve(
            ApproveSellerOnboardingCommand.from(onboardingId, reviewerId)));

    assertEquals(SellerErrorCode.SELLER_ONBOARDING_NOT_FOUND, exception.getErrorCode());
    verify(sellerOnboardingPersistencePort).findById(onboardingId);
    verifyNoMoreInteractions(sellerOnboardingPersistencePort);
  }

  @Test
  @DisplayName("이미 처리된 입점 신청은 심사할 수 없다")
  void rejectsReviewForAlreadyReviewedOnboarding() {
    UUID onboardingId = UUID.randomUUID();
    UUID reviewerId = UUID.randomUUID();
    SellerOnboarding onboarding = onboarding(onboardingId);
    when(sellerOnboardingPersistencePort.findById(onboardingId))
        .thenReturn(Optional.of(onboarding));
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

  @Test
  @DisplayName("기존 스토어가 있으면 승인만 처리하고 중복 생성하지 않는다")
  void approvesWithoutCreatingDuplicateStore() {
    UUID onboardingId = UUID.randomUUID();
    UUID reviewerId = UUID.randomUUID();
    SellerOnboarding onboarding = onboarding(onboardingId);
    when(sellerOnboardingPersistencePort.findById(onboardingId))
        .thenReturn(Optional.of(onboarding));
    when(sellerOnboardingPersistencePort.approveIfPending(
            eq(onboardingId), eq(reviewerId), any(Instant.class)))
        .thenReturn(true);
    when(storePersistencePort.existsByOwnerUserId(onboarding.getApplicantUserId()))
        .thenReturn(true);

    sellerOnboardingReviewService.approve(
        ApproveSellerOnboardingCommand.from(onboardingId, reviewerId));

    verifyNoMoreInteractions(storeCreateUseCase);
  }

  private SellerOnboarding onboarding(UUID onboardingId) {
    SellerOnboarding onboarding = mock(SellerOnboarding.class);
    when(onboarding.getId()).thenReturn(onboardingId);
    when(onboarding.getApplicantUserId()).thenReturn(UUID.randomUUID());
    when(onboarding.getStoreName()).thenReturn("P3 베이커리");
    when(onboarding.getPhoneNumber()).thenReturn("010-1234-5678");
    when(onboarding.getSnsLink()).thenReturn("https://instagram.com/p3");
    when(onboarding.getAddress()).thenReturn("서울특별시 중구");
    return onboarding;
  }
}
