package io.point3.p3api.seller.infrastructure.persistence;

import io.point3.p3api.seller.application.port.SellerOnboardingPersistencePort;
import io.point3.p3api.seller.domain.entity.SellerOnboarding;
import io.point3.p3api.seller.domain.type.SellerOnboardingStatus;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class SellerOnboardingPersistenceAdapter implements SellerOnboardingPersistencePort {

  private final SellerOnboardingJpaRepository sellerOnboardingJpaRepository;

  @Override
  public SellerOnboarding save(SellerOnboarding sellerOnboarding) {
    return sellerOnboardingJpaRepository.save(sellerOnboarding);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsPendingByApplicantUserId(UUID applicantUserId) {
    return sellerOnboardingJpaRepository.existsByApplicantUserIdAndStatus(
        applicantUserId, SellerOnboardingStatus.PENDING);
  }
}
