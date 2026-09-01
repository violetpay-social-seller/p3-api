package io.point3.p3api.seller.application.review;

import com.fasterxml.jackson.databind.node.TextNode;
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
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SellerOnboardingReviewService implements SellerOnboardingReviewUseCase {

  private final SellerOnboardingPersistencePort sellerOnboardingPersistencePort;
  private final StoreCreateUseCase storeCreateUseCase;
  private final StorePersistencePort storePersistencePort;

  @Override
  public SellerOnboardingReviewResult approve(ApproveSellerOnboardingCommand command) {
    Instant reviewedAt = Instant.now();
    SellerOnboarding onboarding = findOnboarding(command.onboardingId());

    if (!sellerOnboardingPersistencePort.approveIfPending(
        command.onboardingId(), command.reviewerId(), reviewedAt)) {
      throw new BaseException(SellerErrorCode.SELLER_ONBOARDING_REVIEW_NOT_ALLOWED);
    }

    if (!storePersistencePort.existsByOwnerUserId(onboarding.getApplicantUserId())) {
      storeCreateUseCase.create(new CreateStoreCommand(
          onboarding.getApplicantUserId(),
          onboarding.getStoreName(),
          null,
          null,
          onboarding.getPhoneNumber(),
          false,
          onboarding.getSnsLink() == null
              ? null
              : TextNode.valueOf(onboarding.getSnsLink()).toString(),
          null,
          null,
          onboarding.getAddress()));
    }

    return new SellerOnboardingReviewResult(
        command.onboardingId(), SellerOnboardingStatus.APPROVED, command.reviewerId(), reviewedAt);
  }

  @Override
  public SellerOnboardingReviewResult reject(RejectSellerOnboardingCommand command) {
    Instant reviewedAt = Instant.now();
    findOnboarding(command.onboardingId());

    if (!sellerOnboardingPersistencePort.rejectIfPending(
        command.onboardingId(), command.reviewerId(), command.rejectionReason(), reviewedAt)) {
      throw new BaseException(SellerErrorCode.SELLER_ONBOARDING_REVIEW_NOT_ALLOWED);
    }

    return new SellerOnboardingReviewResult(
        command.onboardingId(), SellerOnboardingStatus.REJECTED, command.reviewerId(), reviewedAt);
  }

  private SellerOnboarding findOnboarding(UUID onboardingId) {
    return sellerOnboardingPersistencePort
        .findById(onboardingId)
        .orElseThrow(() -> new BaseException(SellerErrorCode.SELLER_ONBOARDING_NOT_FOUND));
  }
}
