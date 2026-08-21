package io.point3.p3api.seller.infrastructure.persistence;

import io.point3.p3api.seller.domain.entity.SellerOnboarding;
import io.point3.p3api.seller.domain.type.SellerOnboardingStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SellerOnboardingJpaRepository extends JpaRepository<SellerOnboarding, UUID> {

  boolean existsByApplicantUserIdAndStatus(
      UUID applicantUserId, SellerOnboardingStatus sellerOnboardingStatus);

  Optional<SellerOnboarding> findFirstByApplicantUserIdOrderByCreatedAtDesc(UUID applicantUserId);

  Optional<SellerOnboarding> findByIdAndApplicantUserId(UUID id, UUID applicantUserId);

  List<SellerOnboarding> findByStatusOrderByCreatedAtAsc(SellerOnboardingStatus status);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("""
      update SellerOnboarding onboarding
      set onboarding.status = :approvedStatus,
          onboarding.rejectionReason = null,
          onboarding.reviewedBy = :reviewerId,
          onboarding.reviewedAt = :reviewedAt,
          onboarding.updatedAt = :reviewedAt
      where onboarding.id = :onboardingId
        and onboarding.status = :pendingStatus
      """)
  int approveIfPending(
      @Param("onboardingId") UUID onboardingId,
      @Param("reviewerId") UUID reviewerId,
      @Param("reviewedAt") Instant reviewedAt,
      @Param("pendingStatus") SellerOnboardingStatus pendingStatus,
      @Param("approvedStatus") SellerOnboardingStatus approvedStatus);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("""
      update SellerOnboarding onboarding
      set onboarding.status = :rejectedStatus,
          onboarding.rejectionReason = :rejectionReason,
          onboarding.reviewedBy = :reviewerId,
          onboarding.reviewedAt = :reviewedAt,
          onboarding.updatedAt = :reviewedAt
      where onboarding.id = :onboardingId
        and onboarding.status = :pendingStatus
      """)
  int rejectIfPending(
      @Param("onboardingId") UUID onboardingId,
      @Param("reviewerId") UUID reviewerId,
      @Param("rejectionReason") String rejectionReason,
      @Param("reviewedAt") Instant reviewedAt,
      @Param("pendingStatus") SellerOnboardingStatus pendingStatus,
      @Param("rejectedStatus") SellerOnboardingStatus rejectedStatus);
}
