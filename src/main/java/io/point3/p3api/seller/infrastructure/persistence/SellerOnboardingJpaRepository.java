package io.point3.p3api.seller.infrastructure.persistence;

import io.point3.p3api.seller.domain.entity.SellerOnboarding;
import io.point3.p3api.seller.domain.type.SellerOnboardingStatus;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerOnboardingJpaRepository extends JpaRepository<SellerOnboarding, UUID> {

  boolean existsByApplicantUserIdAndStatus(
      UUID applicantUserId, SellerOnboardingStatus sellerOnboardingStatus);
}
