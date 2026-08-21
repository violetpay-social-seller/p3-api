package io.point3.p3api.common.tenant.seller.provider;

import io.point3.p3api.auth.infrastructure.web.CurrentUser;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.SellerErrorCode;
import io.point3.p3api.seller.application.port.SellerOnboardingPersistencePort;
import io.point3.p3api.seller.domain.type.SellerOnboardingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SellerOnboardingApprovalProvider {

  private final SellerOnboardingPersistencePort sellerOnboardingPersistencePort;

  @Transactional(readOnly = true)
  public void requireApproved(CurrentUser currentUser) {
    SellerOnboardingStatus status = sellerOnboardingPersistencePort
        .findLatestByApplicantUserId(currentUser.userId())
        .map(sellerOnboarding -> sellerOnboarding.getStatus())
        .orElseThrow(() -> new BaseException(SellerErrorCode.SELLER_ONBOARDING_APPROVAL_REQUIRED));

    if (status != SellerOnboardingStatus.APPROVED) {
      throw new BaseException(SellerErrorCode.SELLER_ONBOARDING_APPROVAL_REQUIRED);
    }
  }
}
