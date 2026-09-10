package io.point3.p3api.seller.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SellerOnboardingTest {

  @Test
  @DisplayName("기본주소와 상세주소를 정규화해 전체 주소로 조합한다")
  void combinesAddressParts() {
    SellerOnboarding onboarding = SellerOnboarding.create(
        UUID.randomUUID(),
        "테스트 스토어",
        "010-1234-5678",
        " 서울특별시  서대문구 연희로 12길 ",
        " 1층  101호 ",
        "https://instagram.com/test");

    assertEquals("서울특별시 서대문구 연희로 12길", onboarding.getAddress());
    assertEquals("1층 101호", onboarding.getDetailAddress());
    assertEquals("서울특별시 서대문구 연희로 12길 1층 101호", onboarding.getFullAddress());
  }

  @Test
  @DisplayName("인스타그램 ID는 @를 제거해 저장한다")
  void normalizesInstagramUsername() {
    SellerOnboarding onboarding = SellerOnboarding.create(
        UUID.randomUUID(),
        "테스트 스토어",
        "010-1234-5678",
        "서울특별시 서대문구",
        null,
        " @wihada.cake ");

    assertEquals("wihada.cake", onboarding.getSnsLink());
  }

  @Test
  @DisplayName("인스타그램 프로필 URL은 username만 저장한다")
  void normalizesInstagramProfileUrl() {
    SellerOnboarding onboarding = SellerOnboarding.create(
        UUID.randomUUID(),
        "테스트 스토어",
        "010-1234-5678",
        "서울특별시 서대문구",
        null,
        "https://instagram.com/wihada");

    assertEquals("wihada", onboarding.getSnsLink());
  }

  @Test
  @DisplayName("상세주소가 없으면 기본주소만 전체 주소로 반환한다")
  void returnsBaseAddressWithoutDetailAddress() {
    SellerOnboarding onboarding = SellerOnboarding.create(
        UUID.randomUUID(), "테스트 스토어", "010-1234-5678", "서울특별시 서대문구 연희로 12길", null, null);

    assertNull(onboarding.getDetailAddress());
    assertNull(onboarding.getSnsLink());
    assertEquals("서울특별시 서대문구 연희로 12길", onboarding.getFullAddress());
  }
}
