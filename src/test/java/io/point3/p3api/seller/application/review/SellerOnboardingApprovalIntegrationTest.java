package io.point3.p3api.seller.application.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.point3.p3api.IntegrationTestSupport;
import io.point3.p3api.seller.domain.entity.SellerOnboarding;
import io.point3.p3api.seller.domain.type.SellerOnboardingStatus;
import io.point3.p3api.seller.infrastructure.persistence.SellerOnboardingJpaRepository;
import io.point3.p3api.store.domain.entity.Store;
import io.point3.p3api.store.domain.type.StoreStatus;
import io.point3.p3api.store.infrastructure.persistence.StoreJpaRepository;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.SignupProvider;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.infrastructure.persistence.UserJpaRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SellerOnboardingApprovalIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private SellerOnboardingReviewUseCase sellerOnboardingReviewUseCase;

  @Autowired
  private SellerOnboardingJpaRepository sellerOnboardingJpaRepository;

  @Autowired
  private StoreJpaRepository storeJpaRepository;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Test
  @DisplayName("대기 중인 입점 신청을 승인하면 신청 정보로 비활성 스토어를 생성한다")
  void createsInactiveStoreWhenApprovingPendingOnboarding() {
    User seller = saveUser(UserRole.SELLER, "seller");
    User operator = saveUser(UserRole.OPERATOR, "operator");
    SellerOnboarding onboarding =
        sellerOnboardingJpaRepository.saveAndFlush(SellerOnboarding.create(
            seller.getId(), "P3 베이커리", "010-1234-5678", "서울특별시 중구", "https://instagram.com/p3"));

    sellerOnboardingReviewUseCase.approve(
        ApproveSellerOnboardingCommand.from(onboarding.getId(), operator.getId()));

    Store store = storeJpaRepository.findByOwnerUserId(seller.getId()).orElseThrow();
    SellerOnboarding approved =
        sellerOnboardingJpaRepository.findById(onboarding.getId()).orElseThrow();
    assertEquals(SellerOnboardingStatus.APPROVED, approved.getStatus());
    assertEquals("P3 베이커리", store.getName());
    assertEquals("010-1234-5678", store.getContact());
    assertEquals("\"https://instagram.com/p3\"", store.getSnsLinks());
    assertEquals("서울특별시 중구", store.getAddress());
    assertFalse(store.isContactVisible());
    assertEquals(StoreStatus.INACTIVE, store.getStatus());
  }

  private User saveUser(UserRole role, String prefix) {
    return userJpaRepository.saveAndFlush(User.create(
        UUID.randomUUID().toString(),
        uniqueEmail(prefix),
        role == UserRole.OPERATOR ? "운영자" : "판매자",
        role,
        role == UserRole.OPERATOR ? null : "010-0000-0000",
        role == UserRole.OPERATOR ? null : SignupProvider.GOOGLE));
  }
}
