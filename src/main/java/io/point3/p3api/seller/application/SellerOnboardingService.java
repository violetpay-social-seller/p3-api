package io.point3.p3api.seller.application;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.SellerErrorCode;
import io.point3.p3api.seller.application.create.CreateSellerOnboardingCommand;
import io.point3.p3api.seller.application.create.SellerOnboardingCreateUseCase;
import io.point3.p3api.seller.application.port.SellerOnboardingPersistencePort;
import io.point3.p3api.seller.application.result.SellerOnboardingResult;
import io.point3.p3api.seller.domain.entity.SellerOnboarding;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SellerOnboardingService implements SellerOnboardingCreateUseCase {

  private final SellerOnboardingPersistencePort sellerOnboardingPersistencePort;

  @Override
  public SellerOnboardingResult create(CreateSellerOnboardingCommand command) {
    if (sellerOnboardingPersistencePort.existsPendingByApplicantUserId(command.applicantUserId())) {
      throw new BaseException(SellerErrorCode.SELLER_ONBOARDING_PENDING_ALREADY_EXISTS);
    }

    SellerOnboarding sellerOnboarding = SellerOnboarding.create(
        command.applicantUserId(),
        command.storeName(),
        command.phoneNumber(),
        command.address(),
        command.snsLink());

    return SellerOnboardingResult.from(sellerOnboardingPersistencePort.save(sellerOnboarding));
  }
}
