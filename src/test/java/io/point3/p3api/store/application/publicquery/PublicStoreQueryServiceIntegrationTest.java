package io.point3.p3api.store.application.publicquery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.point3.p3api.IntegrationTestSupport;
import io.point3.p3api.asset.domain.entity.Asset;
import io.point3.p3api.asset.infrastructure.persistence.AssetJpaRepository;
import io.point3.p3api.assetvariant.domain.entity.AssetVariant;
import io.point3.p3api.assetvariant.domain.type.AssetVariantType;
import io.point3.p3api.assetvariant.infrastructure.persistence.AssetVariantJpaRepository;
import io.point3.p3api.store.application.StoreService;
import io.point3.p3api.store.application.create.CreateStoreCommand;
import io.point3.p3api.store.application.publicquery.result.PublicStoreResult;
import io.point3.p3api.store.application.result.StoreResult;
import io.point3.p3api.store.domain.entity.Store;
import io.point3.p3api.store.domain.entity.StoreRepresentativeImage;
import io.point3.p3api.store.domain.entity.StoreWeeklyPickupSetting;
import io.point3.p3api.store.infrastructure.persistence.RepresentativeImageJpaRepository;
import io.point3.p3api.store.infrastructure.persistence.StoreJpaRepository;
import io.point3.p3api.store.infrastructure.persistence.StoreWeeklyPickupSettingJpaRepository;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.SignupProvider;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.infrastructure.persistence.UserJpaRepository;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "p3.asset.delivery.base-url=https://assets.example.test")
class PublicStoreQueryServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private PublicStoreQueryService publicStoreQueryService;

  @Autowired
  private StoreService storeService;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Autowired
  private StoreJpaRepository storeJpaRepository;

  @Autowired
  private AssetJpaRepository assetJpaRepository;

  @Autowired
  private AssetVariantJpaRepository assetVariantJpaRepository;

  @Autowired
  private RepresentativeImageJpaRepository representativeImageJpaRepository;

  @Autowired
  private StoreWeeklyPickupSettingJpaRepository storeWeeklyPickupSettingJpaRepository;

  @Test
  @DisplayName("공개 스토어 조회는 READY processed variant URL만 응답한다")
  void getsPublicStoreWithReadyProcessedDeliveryUrls() {
    User seller = saveSeller();
    Asset profileAsset = saveAsset(seller.getId(), "original/profile.png");
    saveVariant(profileAsset, "processed/profile_640.webp");
    StoreResult store = createStore(seller.getId(), profileAsset.getId());
    saveWeeklyPickupSettings(store.id());
    saveCancellationRefundPolicy(store.id());
    activate(store.id());

    Asset readyRepresentativeAsset = saveAsset(seller.getId(), "original/ready.png");
    saveVariant(readyRepresentativeAsset, "processed/ready_640.webp");
    representativeImageJpaRepository.saveAndFlush(
        StoreRepresentativeImage.create(store.id(), readyRepresentativeAsset.getId(), 0));

    Asset notReadyRepresentativeAsset = saveAsset(seller.getId(), "original/not-ready.png");
    representativeImageJpaRepository.saveAndFlush(
        StoreRepresentativeImage.create(store.id(), notReadyRepresentativeAsset.getId(), 1));

    PublicStoreResult result = publicStoreQueryService.getStore(store.id());

    assertEquals(
        "https://assets.example.test/processed/profile_640.webp", result.profileDeliveryUrl());
    assertEquals(1, result.representativeImages().size());
    assertEquals(
        "https://assets.example.test/processed/ready_640.webp",
        result.representativeImages().getFirst().deliveryUrl());
    assertEquals(1, result.representativeImages().getFirst().variants().size());
    assertEquals(
        "MEDIUM", result.representativeImages().getFirst().variants().getFirst().type());
    assertEquals(
        "https://assets.example.test/processed/ready_640.webp",
        result.representativeImages().getFirst().variants().getFirst().deliveryUrl());
    assertEquals("화~일 9:00~20:00 · 월 휴무 · 휴게시간 12:00~13:00", result.businessHours());
    assertEquals("서울특별시 중구 101호", result.address());
    assertEquals("픽업일 7일 전까지 100% 환불\n픽업일 5일 전까지 80% 환불", result.cancellationRefundPolicy());
  }

  private User saveSeller() {
    return userJpaRepository.saveAndFlush(User.create(
        UUID.randomUUID().toString(),
        uniqueEmail("public-store-seller"),
        "판매자",
        UserRole.SELLER,
        "010-0000-0000",
        SignupProvider.GOOGLE));
  }

  private StoreResult createStore(UUID ownerUserId, UUID profileAssetId) {
    return storeService.create(new CreateStoreCommand(
        ownerUserId,
        "P3 공개 스토어",
        profileAssetId,
        "주문제작 케이크 스토어",
        "010-1234-5678",
        true,
        "{}",
        "{}",
        "{}",
        "서울특별시 중구",
        "101호"));
  }

  private void activate(UUID storeId) {
    Store store = storeJpaRepository.findById(storeId).orElseThrow();
    store.active();
    storeJpaRepository.saveAndFlush(store);
  }

  private void saveCancellationRefundPolicy(UUID storeId) {
    Store store = storeJpaRepository.findById(storeId).orElseThrow();
    store.updateCancellationRefundPolicy("픽업일 7일 전까지 100% 환불\n픽업일 5일 전까지 80% 환불");
    storeJpaRepository.saveAndFlush(store);
  }

  private Asset saveAsset(UUID uploadedBy, String objectKey) {
    return assetJpaRepository.saveAndFlush(
        Asset.create(UUID.randomUUID(), uploadedBy, "cake.png", "image/png", 1024, objectKey));
  }

  private void saveVariant(Asset asset, String objectKey) {
    assetVariantJpaRepository.saveAndFlush(AssetVariant.create(
        asset, AssetVariantType.MEDIUM, objectKey, "image/webp", 640, 640, 512));
  }

  private void saveWeeklyPickupSettings(UUID storeId) {
    storeWeeklyPickupSettingJpaRepository.saveAllAndFlush(List.of(
        weeklyPickupSetting(storeId, DayOfWeek.MONDAY, false),
        weeklyPickupSetting(storeId, DayOfWeek.TUESDAY, true),
        weeklyPickupSetting(storeId, DayOfWeek.WEDNESDAY, true),
        weeklyPickupSetting(storeId, DayOfWeek.THURSDAY, true),
        weeklyPickupSetting(storeId, DayOfWeek.FRIDAY, true),
        weeklyPickupSetting(storeId, DayOfWeek.SATURDAY, true),
        weeklyPickupSetting(storeId, DayOfWeek.SUNDAY, true)));
  }

  private StoreWeeklyPickupSetting weeklyPickupSetting(
      UUID storeId, DayOfWeek dayOfWeek, boolean enabled) {
    return StoreWeeklyPickupSetting.create(
        storeId,
        dayOfWeek,
        LocalTime.of(9, 0),
        LocalTime.of(20, 0),
        10,
        LocalTime.of(12, 0),
        LocalTime.of(13, 0),
        enabled);
  }
}
