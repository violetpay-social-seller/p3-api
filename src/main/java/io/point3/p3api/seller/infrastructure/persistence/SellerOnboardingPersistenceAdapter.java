package io.point3.p3api.seller.infrastructure.persistence;

import io.point3.p3api.seller.application.port.SellerOnboardingPersistencePort;
import io.point3.p3api.seller.domain.entity.SellerOnboarding;
import io.point3.p3api.seller.domain.type.SellerOnboardingStatus;
import java.time.Instant;
import java.util.List;
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

  @Override
  @Transactional(readOnly = true)
  public boolean existsById(UUID onboardingId) {
    return sellerOnboardingJpaRepository.existsById(onboardingId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<SellerOnboarding> findPendingOrderByCreatedAtAsc() {
    return sellerOnboardingJpaRepository.findByStatusOrderByCreatedAtAsc(
        SellerOnboardingStatus.PENDING);
  }

  @Override
  public boolean approveIfPending(UUID onboardingId, UUID reviewerId, Instant reviewedAt) {
    return sellerOnboardingJpaRepository.approveIfPending(
        onboardingId,
        reviewerId,
        reviewedAt,
        SellerOnboardingStatus.PENDING,
        SellerOnboardingStatus.APPROVED) == 1;
  }

  @Override
  public boolean rejectIfPending(
      UUID onboardingId, UUID reviewerId, String rejectionReason, Instant reviewedAt) {
    return sellerOnboardingJpaRepository.rejectIfPending(
        onboardingId,
        reviewerId,
        rejectionReason,
        reviewedAt,
        SellerOnboardingStatus.PENDING,
        SellerOnboardingStatus.REJECTED) == 1;
  }
}
