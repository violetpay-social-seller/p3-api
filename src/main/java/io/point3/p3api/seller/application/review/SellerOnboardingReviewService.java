package io.point3.p3api.seller.application.review;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.SellerErrorCode;
import io.point3.p3api.seller.application.port.SellerOnboardingPersistencePort;
import io.point3.p3api.seller.application.result.SellerOnboardingReviewResult;
import io.point3.p3api.seller.domain.type.SellerOnboardingStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SellerOnboardingReviewService implements SellerOnboardingReviewUseCase {

  private final SellerOnboardingPersistencePort sellerOnboardingPersistencePort;

  @Override
  public SellerOnboardingReviewResult approve(ApproveSellerOnboardingCommand command) {
    Instant reviewedAt = Instant.now();
    validateOnboardingExists(command.onboardingId());

    if (!sellerOnboardingPersistencePort.approveIfPending(
        command.onboardingId(), command.reviewerId(), reviewedAt)) {
      throw new BaseException(SellerErrorCode.SELLER_ONBOARDING_REVIEW_NOT_ALLOWED);
    }

    return new SellerOnboardingReviewResult(
        command.onboardingId(), SellerOnboardingStatus.APPROVED, command.reviewerId(), reviewedAt);
  }

  @Override
  public SellerOnboardingReviewResult reject(RejectSellerOnboardingCommand command) {
    Instant reviewedAt = Instant.now();
    validateOnboardingExists(command.onboardingId());

    if (!sellerOnboardingPersistencePort.rejectIfPending(
        command.onboardingId(), command.reviewerId(), command.rejectionReason(), reviewedAt)) {
      throw new BaseException(SellerErrorCode.SELLER_ONBOARDING_REVIEW_NOT_ALLOWED);
    }

    return new SellerOnboardingReviewResult(
        command.onboardingId(), SellerOnboardingStatus.REJECTED, command.reviewerId(), reviewedAt);
  }

  private void validateOnboardingExists(UUID onboardingId) {
    if (!sellerOnboardingPersistencePort.existsById(onboardingId)) {
      throw new BaseException(SellerErrorCode.SELLER_ONBOARDING_NOT_FOUND);
    }
  }
}
