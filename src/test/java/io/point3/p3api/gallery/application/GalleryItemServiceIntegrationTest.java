package io.point3.p3api.gallery.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.point3.p3api.IntegrationTestSupport;
import io.point3.p3api.asset.domain.entity.Asset;
import io.point3.p3api.asset.infrastructure.persistence.AssetJpaRepository;
import io.point3.p3api.assetvariant.domain.entity.AssetVariant;
import io.point3.p3api.assetvariant.domain.type.AssetVariantType;
import io.point3.p3api.assetvariant.infrastructure.persistence.AssetVariantJpaRepository;
import io.point3.p3api.gallery.application.command.CreateGalleryItemCommand;
import io.point3.p3api.gallery.application.command.UpdateGalleryItemCommand;
import io.point3.p3api.gallery.application.result.GalleryItemResult;
import io.point3.p3api.gallery.domain.type.StoreGalleryItemStatus;
import io.point3.p3api.store.application.StoreService;
import io.point3.p3api.store.application.create.CreateStoreCommand;
import io.point3.p3api.store.application.result.StoreResult;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.SignupProvider;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.infrastructure.persistence.UserJpaRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "p3.asset.delivery.base-url=https://assets.example.test")
class GalleryItemServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private GalleryItemService galleryItemService;

  @Autowired
  private StoreService storeService;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Autowired
  private AssetJpaRepository assetJpaRepository;

  @Autowired
  private AssetVariantJpaRepository assetVariantJpaRepository;

  @Test
  @DisplayName("갤러리 조회는 processed medium variant delivery URL을 우선 응답한다")
  void getsGalleryItemWithProcessedDeliveryUrl() {
    User seller = saveSeller();
    StoreResult store = createStore(seller.getId());
    Asset asset = saveAsset(seller.getId(), "assets/original/cake.png");
    assetVariantJpaRepository.saveAll(List.of(
        variant(asset, AssetVariantType.THUMBNAIL, "assets/thumb/cake_320.webp", 320),
        variant(asset, AssetVariantType.MEDIUM, "assets/card/cake_640.webp", 640)));
    GalleryItemResult created = galleryItemService.create(
        new CreateGalleryItemCommand(store.id(), asset.getId(), 0, false));
    galleryItemService.update(new UpdateGalleryItemCommand(
        store.id(), created.id(), 0, false, StoreGalleryItemStatus.VISIBLE));

    GalleryItemResult result = galleryItemService.getVisibleItems(store.id()).getFirst();

    assertEquals("https://assets.example.test/assets/card/cake_640.webp", result.deliveryUrl());
    assertEquals(2, result.variants().size());
    assertEquals("THUMBNAIL", result.variants().get(0).type());
    assertEquals(320, result.variants().get(0).width());
    assertEquals("MEDIUM", result.variants().get(1).type());
    assertEquals(640, result.variants().get(1).width());
  }

  @Test
  @DisplayName("processed variant가 없으면 delivery URL을 응답하지 않는다")
  void doesNotExposeOriginalDeliveryUrl() {
    User seller = saveSeller();
    StoreResult store = createStore(seller.getId());
    Asset asset = saveAsset(seller.getId(), "assets/original/cake.png");
    GalleryItemResult created = galleryItemService.create(
        new CreateGalleryItemCommand(store.id(), asset.getId(), 0, false));
    galleryItemService.update(new UpdateGalleryItemCommand(
        store.id(), created.id(), 0, false, StoreGalleryItemStatus.VISIBLE));

    GalleryItemResult result = galleryItemService.getVisibleItems(store.id()).getFirst();

    assertNull(result.deliveryUrl());
    assertTrue(result.variants().isEmpty());
  }

  private User saveSeller() {
    return userJpaRepository.saveAndFlush(User.create(
        UUID.randomUUID().toString(),
        uniqueEmail("gallery-seller"),
        "판매자",
        UserRole.SELLER,
        "010-0000-0000",
        SignupProvider.GOOGLE));
  }

  private StoreResult createStore(UUID ownerUserId) {
    return storeService.create(new CreateStoreCommand(
        ownerUserId,
        "P3 갤러리",
        null,
        "주문제작 케이크 스토어",
        "010-1234-5678",
        true,
        "{}",
        "{}",
        "{}",
        "서울특별시 중구"));
  }

  private Asset saveAsset(UUID uploadedBy, String objectKey) {
    return assetJpaRepository.saveAndFlush(
        Asset.create(UUID.randomUUID(), uploadedBy, "cake.png", "image/png", 1024, objectKey));
  }

  private AssetVariant variant(Asset asset, AssetVariantType type, String objectKey, int width) {
    return AssetVariant.create(asset, type, objectKey, "image/webp", width, width, 512);
  }
}
