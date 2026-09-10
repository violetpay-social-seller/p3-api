package io.point3.p3api.store.application.profileimage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.point3.p3api.IntegrationTestSupport;
import io.point3.p3api.asset.domain.entity.Asset;
import io.point3.p3api.asset.infrastructure.persistence.AssetJpaRepository;
import io.point3.p3api.assetvariant.domain.entity.AssetVariant;
import io.point3.p3api.assetvariant.domain.type.AssetVariantType;
import io.point3.p3api.assetvariant.infrastructure.persistence.AssetVariantJpaRepository;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.StoreErrorCode;
import io.point3.p3api.store.domain.entity.Store;
import io.point3.p3api.store.infrastructure.persistence.StoreJpaRepository;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.SignupProvider;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.infrastructure.persistence.UserJpaRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "p3.asset.delivery.base-url=https://assets.example.test")
class SellerProfileImageServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private SellerProfileImageService sellerProfileImageService;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Autowired
  private StoreJpaRepository storeJpaRepository;

  @Autowired
  private AssetJpaRepository assetJpaRepository;

  @Autowired
  private AssetVariantJpaRepository assetVariantJpaRepository;

  @Test
  @DisplayName("판매자 프로필 이미지는 계정과 스토어에 함께 반영된다")
  void updatesUserAndStoreProfileImage() {
    User seller = saveSeller();
    Store store = saveStore(seller);
    Asset asset = saveAsset(seller.getId());
    AssetVariant variant = saveVariant(asset);

    SellerProfileImageResult result = sellerProfileImageService.update(
        new UpdateSellerProfileImageCommand(store.getId(), seller.getId(), asset.getId()));

    assertEquals(asset.getId(), result.profileAssetId());
    assertEquals(
        "https://assets.example.test/" + variant.getObjectKey(), result.profileImageDeliveryUrl());
    assertEquals(
        asset.getId(),
        userJpaRepository.findById(seller.getId()).orElseThrow().getProfileAssetId());
    assertEquals(
        asset.getId(),
        storeJpaRepository.findById(store.getId()).orElseThrow().getProfileAssetId());
  }

  @Test
  @DisplayName("다른 사용자의 Asset은 판매자 프로필 이미지로 설정할 수 없다")
  void rejectsAssetOwnedByAnotherUser() {
    User seller = saveSeller();
    User anotherSeller = saveSeller();
    Store store = saveStore(seller);
    Asset asset = saveAsset(anotherSeller.getId());

    BaseException exception = assertThrows(
        BaseException.class,
        () -> sellerProfileImageService.update(
            new UpdateSellerProfileImageCommand(store.getId(), seller.getId(), asset.getId())));

    assertEquals(StoreErrorCode.PROFILE_ASSET_NOT_FOUND, exception.getErrorCode());
    assertNull(userJpaRepository.findById(seller.getId()).orElseThrow().getProfileAssetId());
    assertNull(storeJpaRepository.findById(store.getId()).orElseThrow().getProfileAssetId());
  }

  @Test
  @DisplayName("판매자 프로필 이미지를 해제하면 계정과 스토어에서 함께 제거된다")
  void clearsUserAndStoreProfileImage() {
    User seller = saveSeller();
    Asset asset = saveAsset(seller.getId());
    seller.updateProfileAsset(asset.getId());
    userJpaRepository.saveAndFlush(seller);
    Store store = saveStore(seller);
    store.updateProfileAsset(asset.getId());
    storeJpaRepository.saveAndFlush(store);

    SellerProfileImageResult result = sellerProfileImageService.update(
        new UpdateSellerProfileImageCommand(store.getId(), seller.getId(), null));

    assertNull(result.profileAssetId());
    assertNull(result.profileImageDeliveryUrl());
    assertNull(userJpaRepository.findById(seller.getId()).orElseThrow().getProfileAssetId());
    assertNull(storeJpaRepository.findById(store.getId()).orElseThrow().getProfileAssetId());
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

  private Store saveStore(User seller) {
    return storeJpaRepository.saveAndFlush(
        Store.create(seller.getId(), "P3 베이커리", "p3-" + UUID.randomUUID()));
  }

  private Asset saveAsset(UUID uploadedBy) {
    UUID assetId = UUID.randomUUID();
    return assetJpaRepository.saveAndFlush(Asset.create(
        assetId,
        uploadedBy,
        "profile.png",
        "image/png",
        1024,
        "original/" + assetId + "/profile.png"));
  }

  private AssetVariant saveVariant(Asset asset) {
    return assetVariantJpaRepository.saveAndFlush(AssetVariant.create(
        asset,
        AssetVariantType.MEDIUM,
        "processed/" + asset.getId() + "/profile.webp",
        "image/webp",
        640,
        640,
        1024));
  }
}
