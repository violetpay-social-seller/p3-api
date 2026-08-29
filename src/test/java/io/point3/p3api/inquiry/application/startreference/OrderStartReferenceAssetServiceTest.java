package io.point3.p3api.inquiry.application.startreference;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
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
  private final OrderStartReferenceAssetService service =
      new OrderStartReferenceAssetService(persistencePort, new ObjectMapper());

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
        List.of(new OrderFormDraftData.ReferenceAsset(
            firstAssetId, OrderFormReferenceAssetSource.STORE_GALLERY, 0)));
    service.replaceIfPresent(
        inquiryId,
        buyerId,
        List.of(new OrderFormDraftData.ReferenceAsset(
            secondAssetId, OrderFormReferenceAssetSource.USER_UPLOAD, 0)));

    assertEquals(1, service.findAllByInquiryId(inquiryId).size());
    assertEquals(secondAssetId, service.findAllByInquiryId(inquiryId).get(0).assetId());
  }

  @Test
  @DisplayName("빈 주문 시작 참조 이미지는 기존 값을 유지한다")
  void keepsExistingAssetsWhenEmpty() {
    UUID inquiryId = UUID.randomUUID();
    UUID buyerId = UUID.randomUUID();
    UUID assetId = UUID.randomUUID();

    service.replaceIfPresent(
        inquiryId,
        buyerId,
        List.of(new OrderFormDraftData.ReferenceAsset(
            assetId, OrderFormReferenceAssetSource.STORE_GALLERY, 0)));
    service.replaceIfPresent(inquiryId, buyerId, List.of());

    assertEquals(assetId, service.findAllByInquiryId(inquiryId).get(0).assetId());
  }

  @Test
  @DisplayName("주문 생성용 스냅샷은 정렬된 assetId 배열이다")
  void createsOrderAssetIdSnapshot() {
    UUID inquiryId = UUID.randomUUID();
    UUID buyerId = UUID.randomUUID();
    UUID firstAssetId = UUID.randomUUID();
    UUID secondAssetId = UUID.randomUUID();

    service.replaceIfPresent(
        inquiryId,
        buyerId,
        List.of(
            new OrderFormDraftData.ReferenceAsset(
                secondAssetId, OrderFormReferenceAssetSource.USER_UPLOAD, 1),
            new OrderFormDraftData.ReferenceAsset(
                firstAssetId, OrderFormReferenceAssetSource.STORE_GALLERY, 0)));

    assertEquals(
        "[\"" + firstAssetId + "\",\"" + secondAssetId + "\"]",
        service.createOrderAssetIdSnapshot(inquiryId));
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
}
