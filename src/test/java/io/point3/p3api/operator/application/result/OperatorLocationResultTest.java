package io.point3.p3api.operator.application.result;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.point3.p3api.seller.domain.entity.SellerOnboarding;
import io.point3.p3api.store.domain.entity.Store;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OperatorLocationResultTest {

  @Test
  @DisplayName("운영자 입점 신청 조회는 기본주소와 상세주소를 합쳐 반환한다")
  void returnsFullOnboardingAddress() {
    SellerOnboarding onboarding = SellerOnboarding.create(
        UUID.randomUUID(),
        "P3 베이커리",
        "010-1234-5678",
        "서울특별시 중구",
        "101호",
        null);

    OperatorOnboardingResult result = OperatorOnboardingResult.from(onboarding);

    assertEquals("서울특별시 중구 101호", result.address());
  }

  @Test
  @DisplayName("운영자 스토어 조회는 기본주소와 상세주소를 합쳐 반환한다")
  void returnsFullStoreAddress() {
    Store store = Store.create(UUID.randomUUID(), "P3 베이커리", "p3-bakery");
    store.initializeLocation("서울특별시 중구", "101호");

    OperatorStoreResult result = OperatorStoreResult.from(store);

    assertEquals("서울특별시 중구 101호", result.address());
  }
}
