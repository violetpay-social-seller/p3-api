package io.point3.p3api.store.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.point3.p3api.IntegrationTestSupport;
import io.point3.p3api.asset.domain.entity.Asset;
import io.point3.p3api.asset.infrastructure.persistence.AssetJpaRepository;
import io.point3.p3api.assetvariant.domain.entity.AssetVariant;
import io.point3.p3api.assetvariant.domain.type.AssetVariantType;
import io.point3.p3api.assetvariant.infrastructure.persistence.AssetVariantJpaRepository;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.AssetErrorCode;
import io.point3.p3api.exception.code.StoreErrorCode;
import io.point3.p3api.orderform.domain.entity.OrderFormTemplate;
import io.point3.p3api.orderform.infrastructure.persistence.OrderFormTemplateJpaRepository;
import io.point3.p3api.store.application.create.CreateStoreCommand;
import io.point3.p3api.store.application.notice.port.StoreNoticePersistencePort;
import io.point3.p3api.store.application.representative.RepresentativeImageService;
import io.point3.p3api.store.application.representative.command.CreateRepresentativeImageCommand;
import io.point3.p3api.store.application.representative.command.UpdateRepresentativeImageCommand;
import io.point3.p3api.store.application.representative.result.RepresentativeImageResult;
import io.point3.p3api.store.application.result.StoreResult;
import io.point3.p3api.store.application.update.ChangeStoreStatusCommand;
import io.point3.p3api.store.application.update.UpdateStoreCommand;
import io.point3.p3api.store.domain.entity.Store;
import io.point3.p3api.store.domain.entity.StoreNotice;
import io.point3.p3api.store.domain.entity.StoreOperationSetting;
import io.point3.p3api.store.domain.entity.StoreWeeklyPickupSetting;
import io.point3.p3api.store.domain.type.StoreNoticeType;
import io.point3.p3api.store.domain.type.StoreRepresentativeImageStatus;
import io.point3.p3api.store.domain.type.StoreStatus;
import io.point3.p3api.store.infrastructure.persistence.StoreJpaRepository;
import io.point3.p3api.store.infrastructure.persistence.StoreOperationSettingJpaRepository;
import io.point3.p3api.store.infrastructure.persistence.StoreWeeklyPickupSettingJpaRepository;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.SignupProvider;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.infrastructure.persistence.UserJpaRepository;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "p3.asset.delivery.base-url=https://assets.example.test")
class StoreServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private StoreService storeService;

  @Autowired
  private RepresentativeImageService representativeImageService;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Autowired
  private StoreJpaRepository storeJpaRepository;

  @Autowired
  private AssetJpaRepository assetJpaRepository;

  @Autowired
  private AssetVariantJpaRepository assetVariantJpaRepository;

  @Autowired
  private OrderFormTemplateJpaRepository orderFormTemplateJpaRepository;

  @Autowired
  private StoreOperationSettingJpaRepository storeOperationSettingJpaRepository;

  @Autowired
  private StoreWeeklyPickupSettingJpaRepository storeWeeklyPickupSettingJpaRepository;

  @Autowired
  private StoreNoticePersistencePort storeNoticePersistencePort;

  @Test
  @DisplayName("스토어 생성은 실제 저장소에서 판매자 1명당 1개 제약을 검증한다")
  void rejectsDuplicateStoreForSameOwner() {
    User seller = saveSeller();
    storeService.create(createStoreCommand(seller.getId(), "P3 베이커리"));

    BaseException exception = assertThrows(
        BaseException.class,
        () -> storeService.create(createStoreCommand(seller.getId(), "다른 베이커리")));

    assertEquals(StoreErrorCode.STORE_ALREADY_EXISTS, exception.getErrorCode());
  }

  @Test
  @DisplayName("스토어 생성은 프로필 Asset 소유권을 검증한다")
  void rejectsProfileAssetOwnedByAnotherUserOnCreate() {
    User seller = saveSeller();
    User anotherSeller = saveSeller();
    Asset profileAsset = saveAsset(anotherSeller.getId(), 0);

    BaseException exception = assertThrows(
        BaseException.class,
        () -> storeService.create(
            createStoreCommand(seller.getId(), "P3 베이커리", profileAsset.getId())));

    assertEquals(StoreErrorCode.PROFILE_ASSET_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("스토어 수정은 프로필 Asset 소유권을 검증한다")
  void rejectsProfileAssetOwnedByAnotherUserOnUpdate() {
    User seller = saveSeller();
    User anotherSeller = saveSeller();
    StoreResult store = storeService.create(createStoreCommand(seller.getId(), "P3 베이커리"));
    Asset profileAsset = saveAsset(anotherSeller.getId(), 0);

    BaseException exception = assertThrows(
        BaseException.class,
        () -> storeService.update(updateStoreCommand(store.id(), profileAsset.getId())));

    assertEquals(StoreErrorCode.PROFILE_ASSET_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("processed variant가 없으면 대표 이미지를 생성할 수 없다")
  void rejectsRepresentativeImageWithoutProcessedVariant() {
    User seller = saveSeller();
    StoreResult store = storeService.create(createStoreCommand(seller.getId(), "P3 베이커리"));
    Asset asset = saveAsset(seller.getId(), 0);

    BaseException exception = assertThrows(
        BaseException.class,
        () -> representativeImageService.create(
            new CreateRepresentativeImageCommand(store.id(), asset.getId(), 0)));

    assertEquals(AssetErrorCode.ASSET_VARIANT_NOT_READY, exception.getErrorCode());
  }

  @Test
  @DisplayName("대표 이미지 응답은 processed variant delivery URL을 포함한다")
  void createsRepresentativeImageWithDeliveryUrl() {
    User seller = saveSeller();
    StoreResult store = storeService.create(createStoreCommand(seller.getId(), "P3 베이커리"));
    Asset asset = saveAsset(seller.getId(), 0);
    AssetVariant variant = saveVariant(asset, 0);

    RepresentativeImageResult result = representativeImageService.create(
        new CreateRepresentativeImageCommand(store.id(), asset.getId(), 0));

    String deliveryUrl = "https://assets.example.test/" + variant.getObjectKey();
    assertEquals(1, result.variants().size());
    assertEquals(deliveryUrl, result.deliveryUrl());
    assertEquals("MEDIUM", result.variants().getFirst().type());
    assertEquals(deliveryUrl, result.variants().getFirst().deliveryUrl());
  }

  @Test
  @DisplayName("대표이미지가 3개 미만이면 스토어 활성화를 거절한다")
  void rejectsActivationWhenRepresentativeImagesAreLessThanThree() {
    User seller = saveSeller();
    StoreResult store = storeService.create(createStoreCommand(seller.getId(), "P3 베이커리"));
    createRepresentativeImage(store.id(), seller.getId(), 0);
    createRepresentativeImage(store.id(), seller.getId(), 1);

    BaseException exception = assertThrows(
        BaseException.class,
        () -> storeService.changeStatus(
            new ChangeStoreStatusCommand(store.id(), StoreStatus.ACTIVE)));

    assertEquals(StoreErrorCode.REPRESENTATIVE_IMAGE_MINIMUM_REQUIRED, exception.getErrorCode());
  }

  @Test
  @DisplayName("활성 주문서가 없으면 스토어 활성화를 거절한다")
  void rejectsActivationWhenActiveOrderFormIsMissing() {
    User seller = saveSeller();
    StoreResult store = storeService.create(createStoreCommand(seller.getId(), "P3 베이커리"));
    createRepresentativeImage(store.id(), seller.getId(), 0);
    createRepresentativeImage(store.id(), seller.getId(), 1);
    createRepresentativeImage(store.id(), seller.getId(), 2);
    prepareOperationSettingAndSettlementAccount(store.id());

    BaseException exception = assertThrows(
        BaseException.class,
        () -> storeService.changeStatus(
            new ChangeStoreStatusCommand(store.id(), StoreStatus.ACTIVE)));

    assertEquals(StoreErrorCode.ACTIVE_ORDER_FORM_REQUIRED, exception.getErrorCode());
  }

  @Test
  @DisplayName("대표이미지가 3개 이상이면 스토어를 활성화한다")
  void activatesStoreWhenRepresentativeImagesAreReady() {
    User seller = saveSeller();
    StoreResult store = storeService.create(createStoreCommand(seller.getId(), "P3 베이커리"));
    createRepresentativeImage(store.id(), seller.getId(), 0);
    createRepresentativeImage(store.id(), seller.getId(), 1);
    createRepresentativeImage(store.id(), seller.getId(), 2);
    prepareForActivation(store.id());

    StoreResult activated =
        storeService.changeStatus(new ChangeStoreStatusCommand(store.id(), StoreStatus.ACTIVE));

    Store persisted = storeJpaRepository.findById(store.id()).orElseThrow();
    assertEquals(StoreStatus.ACTIVE, activated.status());
    assertEquals(StoreStatus.ACTIVE, persisted.getStatus());
  }

  @Test
  @DisplayName("활성 스토어는 대표이미지가 3개 아래로 줄어드는 숨김을 거절한다")
  void rejectsHidingRepresentativeImageBelowMinimumForActiveStore() {
    User seller = saveSeller();
    StoreResult store = storeService.create(createStoreCommand(seller.getId(), "P3 베이커리"));
    RepresentativeImageResult first = createRepresentativeImage(store.id(), seller.getId(), 0);
    createRepresentativeImage(store.id(), seller.getId(), 1);
    createRepresentativeImage(store.id(), seller.getId(), 2);
    prepareForActivation(store.id());
    storeService.changeStatus(new ChangeStoreStatusCommand(store.id(), StoreStatus.ACTIVE));

    BaseException exception = assertThrows(
        BaseException.class,
        () -> representativeImageService.update(new UpdateRepresentativeImageCommand(
            store.id(), first.id(), 0, StoreRepresentativeImageStatus.HIDDEN)));

    assertEquals(StoreErrorCode.REPRESENTATIVE_IMAGE_MINIMUM_REQUIRED, exception.getErrorCode());
  }

  private User saveSeller() {
    return userJpaRepository.saveAndFlush(User.create(
        UUID.randomUUID().toString(),
        uniqueEmail("seller"),
        "판매자",
        UserRole.SELLER,
        "010-0000-0000",
        SignupProvider.GOOGLE));
  }

  private CreateStoreCommand createStoreCommand(UUID ownerUserId, String name) {
    return createStoreCommand(ownerUserId, name, null);
  }

  private CreateStoreCommand createStoreCommand(
      UUID ownerUserId, String name, UUID profileAssetId) {
    return new CreateStoreCommand(
        ownerUserId,
        name,
        profileAssetId,
        "주문제작 케이크 스토어",
        "010-1234-5678",
        true,
        "{\"instagram\":\"https://instagram.com/p3bakery\"}",
        "{\"mon\":\"10:00-18:00\"}",
        "{\"leadTimeDays\":3}",
        "서울특별시 중구");
  }

  private UpdateStoreCommand updateStoreCommand(UUID storeId, UUID profileAssetId) {
    return new UpdateStoreCommand(
        storeId,
        "P3 베이커리",
        profileAssetId,
        "주문제작 케이크 스토어",
        "010-1234-5678",
        true,
        "{\"instagram\":\"https://instagram.com/p3bakery\"}",
        "{\"mon\":\"10:00-18:00\"}",
        "{\"leadTimeDays\":3}",
        "서울특별시 중구");
  }

  private RepresentativeImageResult createRepresentativeImage(
      UUID storeId, UUID uploadedBy, int sortOrder) {
    Asset asset = saveAsset(uploadedBy, sortOrder);
    saveVariant(asset, sortOrder);

    return representativeImageService.create(
        new CreateRepresentativeImageCommand(storeId, asset.getId(), sortOrder));
  }

  private Asset saveAsset(UUID uploadedBy, int sortOrder) {
    return assetJpaRepository.saveAndFlush(Asset.create(
        UUID.randomUUID(),
        uploadedBy,
        "cake-" + sortOrder + ".png",
        "image/png",
        1024,
        "original/" + UUID.randomUUID() + "/cake-" + sortOrder + ".png"));
  }

  private AssetVariant saveVariant(Asset asset, int sortOrder) {
    return assetVariantJpaRepository.saveAndFlush(AssetVariant.create(
        asset,
        AssetVariantType.MEDIUM,
        "processed/" + UUID.randomUUID() + "/cake-" + sortOrder + ".webp",
        "image/webp",
        640,
        640,
        512));
  }

  private void prepareForActivation(UUID storeId) {
    orderFormTemplateJpaRepository.saveAndFlush(OrderFormTemplate.create(storeId, "기본 주문서"));
    storeNoticePersistencePort.replaceAllByStoreId(
        storeId,
        Arrays.stream(StoreNoticeType.values())
            .map(type -> StoreNotice.create(storeId, type, "안내"))
            .toList());
    prepareOperationSettingAndSettlementAccount(storeId);
  }

  private void prepareOperationSettingAndSettlementAccount(UUID storeId) {
    storeOperationSettingJpaRepository.saveAndFlush(
        StoreOperationSetting.create(storeId, 60, "주문 전 안내", 0));
    storeWeeklyPickupSettingJpaRepository.saveAndFlush(StoreWeeklyPickupSetting.create(
        storeId, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(18, 0), 10, true));
    Store store = storeJpaRepository.findById(storeId).orElseThrow();
    store.markSettlementAccountInputCompleted(Instant.now());
    storeJpaRepository.saveAndFlush(store);
  }
}
