package io.point3.p3api.seller.application.port;

import io.point3.p3api.seller.domain.entity.SellerOnboarding;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SellerOnboardingPersistencePort {

  SellerOnboarding save(SellerOnboarding sellerOnboarding);

  boolean existsPendingByApplicantUserId(UUID applicantUserId);

  Optional<SellerOnboarding> findLatestByApplicantUserId(UUID applicantUserId);

  boolean existsById(UUID onboardingId);

  List<SellerOnboarding> findPendingOrderByCreatedAtAsc();

  boolean approveIfPending(UUID onboardingId, UUID reviewerId, Instant reviewedAt);

  boolean rejectIfPending(
      UUID onboardingId, UUID reviewerId, String rejectionReason, Instant reviewedAt);
}
