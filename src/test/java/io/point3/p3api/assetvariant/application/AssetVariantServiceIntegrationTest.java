package io.point3.p3api.assetvariant.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.point3.p3api.IntegrationTestSupport;
import io.point3.p3api.asset.domain.entity.Asset;
import io.point3.p3api.asset.infrastructure.persistence.AssetJpaRepository;
import io.point3.p3api.assetvariant.application.register.RegisterAssetVariantsCommand;
import io.point3.p3api.assetvariant.application.result.RegisteredAssetVariant;
import io.point3.p3api.assetvariant.application.result.RegisteredAssetVariants;
import io.point3.p3api.assetvariant.domain.type.AssetVariantType;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.AssetErrorCode;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.user.domain.entity.User;
import io.point3.p3api.user.domain.type.SignupProvider;
import io.point3.p3api.user.domain.type.UserRole;
import io.point3.p3api.user.infrastructure.persistence.UserJpaRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "p3.asset.delivery.base-url=https://assets.example.test")
class AssetVariantServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private AssetVariantService assetVariantService;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Autowired
  private AssetJpaRepository assetJpaRepository;

  @Test
  @DisplayName("AssetVariant 등록은 원본 Asset 존재를 검증하고 delivery URL을 응답한다")
  void registersAssetVariants() {
    Asset asset = saveAsset();
    RegisterAssetVariantsCommand.Variant thumbnail =
        variant(AssetVariantType.THUMBNAIL, "processed/thumb.webp", 320, 320);
    RegisterAssetVariantsCommand.Variant medium =
        variant(AssetVariantType.MEDIUM, "processed/medium.webp", 640, 640);

    RegisteredAssetVariants result = assetVariantService.register(
        new RegisterAssetVariantsCommand(asset.getId(), List.of(thumbnail, medium)));

    RegisteredAssetVariants queried = assetVariantService.getVariants(asset.getId());
    assertEquals(asset.getId(), result.assetId());
    Map<AssetVariantType, String> deliveryUrls = result.variants().stream()
        .collect(
            Collectors.toMap(RegisteredAssetVariant::type, RegisteredAssetVariant::deliveryUrl));
    assertEquals(
        "https://assets.example.test/" + thumbnail.objectKey(),
        deliveryUrls.get(AssetVariantType.THUMBNAIL));
    assertEquals(
        "https://assets.example.test/" + medium.objectKey(),
        deliveryUrls.get(AssetVariantType.MEDIUM));
    assertEquals(2, queried.variants().size());
  }

  @Test
  @DisplayName("이미 등록된 타입의 AssetVariant는 중복 등록할 수 없다")
  void rejectsDuplicateVariantType() {
    Asset asset = saveAsset();
    assetVariantService.register(new RegisterAssetVariantsCommand(
        asset.getId(),
        List.of(variant(AssetVariantType.THUMBNAIL, "processed/thumb.webp", 320, 320))));

    BaseException exception = assertThrows(
        BaseException.class,
        () -> assetVariantService.register(new RegisterAssetVariantsCommand(
            asset.getId(),
            List.of(variant(AssetVariantType.THUMBNAIL, "processed/thumb-2.webp", 320, 320)))));

    assertEquals(AssetErrorCode.ASSET_VARIANT_ALREADY_EXISTS, exception.getErrorCode());
  }

  @Test
  @DisplayName("존재하지 않는 Asset의 Variant 조회는 잘못된 ID로 거절한다")
  void rejectsUnknownAssetId() {
    BaseException exception =
        assertThrows(BaseException.class, () -> assetVariantService.getVariants(UUID.randomUUID()));

    assertEquals(CommonErrorCode.INVALID_ID, exception.getErrorCode());
  }

  private Asset saveAsset() {
    User user = userJpaRepository.saveAndFlush(User.create(
        UUID.randomUUID().toString(),
        uniqueEmail("variant-user"),
        "사용자",
        UserRole.SELLER,
        "010-0000-0000",
        SignupProvider.GOOGLE));
    return assetJpaRepository.saveAndFlush(Asset.create(
        UUID.randomUUID(),
        user.getId(),
        "cake.png",
        "image/png",
        1024,
        "original/" + UUID.randomUUID() + "/cake.png"));
  }

  private RegisterAssetVariantsCommand.Variant variant(
      AssetVariantType type, String objectKey, int width, int height) {
    return new RegisterAssetVariantsCommand.Variant(
        type, objectKey + "-" + UUID.randomUUID(), "image/webp", width, height, 512);
  }
}
