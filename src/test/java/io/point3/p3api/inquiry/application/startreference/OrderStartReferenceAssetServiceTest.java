package io.point3.p3api.inquiry.application.startreference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.asset.application.AssetDeliveryUrlResolver;
import io.point3.p3api.asset.domain.entity.Asset;
import io.point3.p3api.assetvariant.application.AssetVariantDeliveryService;
import io.point3.p3api.assetvariant.application.port.AssetVariantPersistencePort;
import io.point3.p3api.assetvariant.domain.entity.AssetVariant;
import io.point3.p3api.assetvariant.domain.type.AssetVariantStatus;
import io.point3.p3api.assetvariant.domain.type.AssetVariantType;
import io.point3.p3api.inquiry.application.draft.model.OrderFormDraftData;
import io.point3.p3api.inquiry.application.port.OrderStartReferenceAssetPersistencePort;
import io.point3.p3api.inquiry.domain.entity.OrderStartReferenceAsset;
import io.point3.p3api.inquiry.domain.type.OrderFormReferenceAssetSource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderStartReferenceAssetServiceTest {

  private final FakeOrderStartReferenceAssetPersistencePort persistencePort =
      new FakeOrderStartReferenceAssetPersistencePort();
  private final FakeAssetVariantPersistencePort assetVariantPersistencePort =
      new FakeAssetVariantPersistencePort();
  private final OrderStartReferenceAssetService service = new OrderStartReferenceAssetService(
      persistencePort,
      new AssetVariantDeliveryService(
          assetVariantPersistencePort, new AssetDeliveryUrlResolver("https://assets.example.test")),
      new ObjectMapper());

  @Test
  @DisplayName("주문 시작 참조 이미지는 문의 단위로 교체 저장된다")
  void replacesStartReferenceAssets() {
    UUID inquiryId = UUID.randomUUID();
    UUID buyerId = UUID.randomUUID();
    UUID firstAssetId = UUID.randomUUID();
    UUID secondAssetId = UUID.randomUUID();

    service.replaceIfPresent(
        inquiryId,
        buyerId,
        new OrderFormDraftData.ReferenceAsset(
            firstAssetId, OrderFormReferenceAssetSource.STORE_GALLERY),
        true);
    service.replaceIfPresent(
        inquiryId,
        buyerId,
        new OrderFormDraftData.ReferenceAsset(
            secondAssetId, OrderFormReferenceAssetSource.USER_UPLOAD),
        true);

    assertEquals(secondAssetId, service.findByInquiryId(inquiryId).assetId());
  }

  @Test
  @DisplayName("주문 시작 참조 이미지가 요청에 없으면 기존 값을 유지한다")
  void keepsExistingAssetsWhenMissing() {
    UUID inquiryId = UUID.randomUUID();
    UUID buyerId = UUID.randomUUID();
    UUID assetId = UUID.randomUUID();

    service.replaceIfPresent(
        inquiryId,
        buyerId,
        new OrderFormDraftData.ReferenceAsset(assetId, OrderFormReferenceAssetSource.STORE_GALLERY),
        true);
    service.replaceIfPresent(inquiryId, buyerId, null, false);

    assertEquals(assetId, service.findByInquiryId(inquiryId).assetId());
  }

  @Test
  @DisplayName("주문 시작 참조 이미지는 문의 단위로 비울 수 있다")
  void clearsStartReferenceAssets() {
    UUID inquiryId = UUID.randomUUID();
    UUID buyerId = UUID.randomUUID();
    UUID assetId = UUID.randomUUID();

    service.replaceIfPresent(
        inquiryId,
        buyerId,
        new OrderFormDraftData.ReferenceAsset(assetId, OrderFormReferenceAssetSource.STORE_GALLERY),
        true);
    service.replaceIfPresent(inquiryId, buyerId, null, true);

    assertNull(service.findByInquiryId(inquiryId));
  }

  @Test
  @DisplayName("준비된 variant가 있으면 주문 시작 참조 이미지 deliveryUrl을 응답한다")
  void returnsDeliveryUrl() {
    UUID inquiryId = UUID.randomUUID();
    UUID buyerId = UUID.randomUUID();
    Asset asset = asset(buyerId);
    assetVariantPersistencePort.saveAll(List.of(AssetVariant.create(
        asset,
        AssetVariantType.MEDIUM,
        "processed/start-reference.webp",
        "image/webp",
        640,
        640,
        512)));

    service.replaceIfPresent(
        inquiryId,
        buyerId,
        new OrderFormDraftData.ReferenceAsset(
            asset.getId(), OrderFormReferenceAssetSource.USER_UPLOAD),
        true);

    assertEquals(
        "https://assets.example.test/processed/start-reference.webp",
        service.findByInquiryId(inquiryId).deliveryUrl());
  }

  @Test
  @DisplayName("준비된 variant가 없으면 주문 시작 참조 이미지 deliveryUrl은 null이다")
  void returnsNullDeliveryUrlWithoutReadyVariant() {
    UUID inquiryId = UUID.randomUUID();
    UUID buyerId = UUID.randomUUID();
    UUID assetId = UUID.randomUUID();

    service.replaceIfPresent(
        inquiryId,
        buyerId,
        new OrderFormDraftData.ReferenceAsset(assetId, OrderFormReferenceAssetSource.USER_UPLOAD),
        true);

    assertNull(service.findByInquiryId(inquiryId).deliveryUrl());
  }

  @Test
  @DisplayName("주문 생성용 스냅샷은 단일 assetId 배열이다")
  void createsOrderAssetIdSnapshot() {
    UUID inquiryId = UUID.randomUUID();
    UUID buyerId = UUID.randomUUID();
    UUID firstAssetId = UUID.randomUUID();

    service.replaceIfPresent(
        inquiryId,
        buyerId,
        new OrderFormDraftData.ReferenceAsset(
            firstAssetId, OrderFormReferenceAssetSource.STORE_GALLERY),
        true);

    assertEquals("[\"" + firstAssetId + "\"]", service.createOrderAssetIdSnapshot(inquiryId));
  }

  private Asset asset(UUID uploadedBy) {
    return Asset.create(
        UUID.randomUUID(),
        uploadedBy,
        "start-reference.png",
        "image/png",
        1024,
        "original/start-reference.png");
  }

  private static class FakeOrderStartReferenceAssetPersistencePort
      implements OrderStartReferenceAssetPersistencePort {

    private final List<OrderStartReferenceAsset> assets = new ArrayList<>();

    @Override
    public List<OrderStartReferenceAsset> saveAll(List<OrderStartReferenceAsset> assets) {
      this.assets.addAll(assets);
      return List.copyOf(assets);
    }

    @Override
    public void deleteAllByInquiryId(UUID inquiryId) {
      assets.removeIf(asset -> asset.getInquiryId().equals(inquiryId));
    }

    @Override
    public List<OrderStartReferenceAsset> findAllByInquiryId(UUID inquiryId) {
      return assets.stream()
          .filter(asset -> asset.getInquiryId().equals(inquiryId))
          .sorted(Comparator.comparing(OrderStartReferenceAsset::getSortOrder))
          .toList();
    }
  }

  private static class FakeAssetVariantPersistencePort implements AssetVariantPersistencePort {

    private final List<AssetVariant> variants = new ArrayList<>();

    @Override
    public List<AssetVariant> saveAll(List<AssetVariant> variants) {
      this.variants.addAll(variants);
      return List.copyOf(variants);
    }

    @Override
    public List<AssetVariant> findAllByAssetId(UUID assetId) {
      return variants.stream()
          .filter(variant -> variant.getAsset().getId().equals(assetId))
          .toList();
    }

    @Override
    public List<AssetVariant> findAllByAssetIds(List<UUID> assetIds) {
      return variants.stream()
          .filter(variant -> assetIds.contains(variant.getAsset().getId()))
          .toList();
    }

    @Override
    public boolean existsByAssetIdAndType(UUID assetId, AssetVariantType type) {
      return variants.stream()
          .anyMatch(
              variant -> variant.getAsset().getId().equals(assetId) && variant.getType() == type);
    }

    @Override
    public boolean existsByAssetIdAndStatus(UUID assetId, AssetVariantStatus status) {
      return variants.stream()
          .anyMatch(variant ->
              variant.getAsset().getId().equals(assetId) && variant.getStatus() == status);
    }
  }
}
