package io.point3.p3api.assetvariant.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.point3.p3api.asset.domain.entity.Asset;
import io.point3.p3api.assetvariant.domain.type.AssetVariantStatus;
import io.point3.p3api.assetvariant.domain.type.AssetVariantType;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AssetVariantTest {

  @Test
  @DisplayName("AssetVariant는 READY로 생성되고 실패 또는 삭제 상태로 전환된다")
  void changesStatus() {
    AssetVariant variant = variant();

    assertEquals(AssetVariantStatus.READY, variant.getStatus());
    variant.markFailed();
    assertEquals(AssetVariantStatus.FAILED, variant.getStatus());
    variant.delete();
    assertEquals(AssetVariantStatus.DELETED, variant.getStatus());
  }

  @Test
  @DisplayName("AssetVariant 크기 정보는 양수여야 한다")
  void rejectsInvalidDimensions() {
    Asset asset = asset();

    assertThrows(
        IllegalArgumentException.class,
        () -> AssetVariant.create(
            asset, AssetVariantType.THUMBNAIL, "processed/thumb.webp", "image/webp", 0, 320, 512));
    assertThrows(
        IllegalArgumentException.class,
        () -> AssetVariant.create(
            asset, AssetVariantType.THUMBNAIL, "processed/thumb.webp", "image/webp", 320, 0, 512));
  }

  private AssetVariant variant() {
    return AssetVariant.create(
        asset(), AssetVariantType.THUMBNAIL, "processed/thumb.webp", "image/webp", 320, 320, 512);
  }

  private Asset asset() {
    return Asset.create(
        UUID.randomUUID(),
        UUID.randomUUID(),
        "cake.png",
        "image/png",
        1024,
        "original/" + UUID.randomUUID() + "/cake.png");
  }
}
