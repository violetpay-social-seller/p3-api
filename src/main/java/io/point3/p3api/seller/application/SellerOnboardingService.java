package io.point3.p3api.seller.application;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.SellerErrorCode;
import io.point3.p3api.seller.application.create.CreateSellerOnboardingCommand;
import io.point3.p3api.seller.application.create.SellerOnboardingCreateUseCase;
import io.point3.p3api.seller.application.port.SellerOnboardingPersistencePort;
import io.point3.p3api.seller.application.query.SellerOnboardingCurrentQueryUseCase;
import io.point3.p3api.seller.application.query.SellerOnboardingPendingQueryUseCase;
import io.point3.p3api.seller.application.reapply.ReapplySellerOnboardingCommand;
import io.point3.p3api.seller.application.reapply.SellerOnboardingReapplicationUseCase;
import io.point3.p3api.seller.application.result.SellerOnboardingDetailResult;
import io.point3.p3api.seller.application.result.SellerOnboardingResult;
import io.point3.p3api.seller.domain.entity.SellerOnboarding;
import io.point3.p3api.seller.domain.type.SellerOnboardingStatus;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SellerOnboardingService
    implements SellerOnboardingCreateUseCase,
        SellerOnboardingPendingQueryUseCase,
        SellerOnboardingCurrentQueryUseCase,
        SellerOnboardingReapplicationUseCase {

  private final SellerOnboardingPersistencePort sellerOnboardingPersistencePort;

  @Override
  public SellerOnboardingResult create(CreateSellerOnboardingCommand command) {
    if (sellerOnboardingPersistencePort.existsPendingByApplicantUserId(command.applicantUserId())) {
      throw new BaseException(SellerErrorCode.SELLER_ONBOARDING_PENDING_ALREADY_EXISTS);
    }

    if (sellerOnboardingPersistencePort
        .findLatestByApplicantUserId(command.applicantUserId())
        .isPresent()) {
      throw new BaseException(SellerErrorCode.SELLER_ONBOARDING_REAPPLICATION_NOT_ALLOWED);
    }

    SellerOnboarding sellerOnboarding = SellerOnboarding.create(
        command.applicantUserId(),
        command.storeName(),
        command.phoneNumber(),
        command.address(),
        command.snsLink());

    return SellerOnboardingResult.from(sellerOnboardingPersistencePort.save(sellerOnboarding));
  }

  @Override
  @Transactional(readOnly = true)
  public List<SellerOnboardingResult> getPendingOnboardings() {
    return sellerOnboardingPersistencePort.findPendingOrderByCreatedAtAsc().stream()
        .map(SellerOnboardingResult::from)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public SellerOnboardingDetailResult getCurrentOnboarding(UUID applicantUserId) {
    return sellerOnboardingPersistencePort
        .findLatestByApplicantUserId(applicantUserId)
        .map(SellerOnboardingDetailResult::from)
        .orElseThrow(() -> new BaseException(SellerErrorCode.SELLER_ONBOARDING_NOT_FOUND));
  }

  @Override
  public SellerOnboardingResult reapply(ReapplySellerOnboardingCommand command) {
    SellerOnboarding onboarding = sellerOnboardingPersistencePort
        .findByIdAndApplicantUserId(command.onboardingId(), command.applicantUserId())
        .orElseThrow(() -> new BaseException(SellerErrorCode.SELLER_ONBOARDING_NOT_FOUND));
    SellerOnboarding latestOnboarding = sellerOnboardingPersistencePort
        .findLatestByApplicantUserId(command.applicantUserId())
        .orElseThrow(() -> new BaseException(SellerErrorCode.SELLER_ONBOARDING_NOT_FOUND));

    if (!latestOnboarding.getId().equals(onboarding.getId())
        || onboarding.getStatus() != SellerOnboardingStatus.REJECTED) {
      throw new BaseException(SellerErrorCode.SELLER_ONBOARDING_REAPPLICATION_NOT_ALLOWED);
    }

    SellerOnboarding reapplication = SellerOnboarding.create(
        command.applicantUserId(),
        command.storeName(),
        command.phoneNumber(),
        command.address(),
        command.snsLink());

    return SellerOnboardingResult.from(sellerOnboardingPersistencePort.save(reapplication));
  }
}
