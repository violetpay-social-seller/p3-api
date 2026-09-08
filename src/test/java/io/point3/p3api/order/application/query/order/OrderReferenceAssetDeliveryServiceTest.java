package io.point3.p3api.order.application.query.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.asset.application.AssetDeliveryUrlResolver;
import io.point3.p3api.asset.application.port.AssetPersistencePort;
import io.point3.p3api.asset.domain.entity.Asset;
import io.point3.p3api.assetvariant.application.AssetVariantDeliveryService;
import io.point3.p3api.assetvariant.application.result.AssetVariantDelivery;
import io.point3.p3api.inquiry.domain.type.OrderFormReferenceAssetSource;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderReferenceAssetDeliveryServiceTest {

  private final AssetPersistencePort assetPersistencePort = mock(AssetPersistencePort.class);
  private final AssetVariantDeliveryService assetVariantDeliveryService =
      mock(AssetVariantDeliveryService.class);
  private final OrderReferenceAssetDeliveryService service =
      new OrderReferenceAssetDeliveryService(
          new ObjectMapper(),
          assetPersistencePort,
          new AssetDeliveryUrlResolver("https://assets.example.test"),
          assetVariantDeliveryService);

  @Test
  @DisplayName("주문 참고 이미지 object 스냅샷에 delivery URL과 variants를 붙인다")
  void appendsDeliveriesToObjectSnapshot() {
    UUID assetId = UUID.fromString("11111111-1111-4111-8111-111111111111");
    Asset asset = asset(assetId);
    String snapshot = """
        [
          {
            "assetId": "11111111-1111-4111-8111-111111111111",
            "source": "STORE_GALLERY",
            "sortOrder": 2
          }
        ]
        """;
    when(assetPersistencePort.findAllById(List.of(assetId))).thenReturn(List.of(asset));
    when(assetVariantDeliveryService.resolveReadyDeliveries(List.of(assetId)))
        .thenReturn(Map.of(
            assetId,
            new AssetVariantDelivery(
                "https://assets.example.test/processed/cake_640.webp",
                List.of(new AssetVariantDelivery.Variant(
                    "MEDIUM", "https://assets.example.test/processed/cake_640.webp", 640, 480)))));

    var results = service.appendDeliveries(snapshot);

    assertEquals(1, results.size());
    assertEquals(assetId, results.getFirst().assetId());
    assertEquals(OrderFormReferenceAssetSource.STORE_GALLERY, results.getFirst().source());
    assertEquals(2, results.getFirst().sortOrder());
    assertEquals("UPLOADED", results.getFirst().status());
    assertEquals(
        "https://assets.example.test/processed/cake_640.webp",
        results.getFirst().deliveryUrl());
    assertEquals(1, results.getFirst().variants().size());
  }

  @Test
  @DisplayName("기존 assetId 배열 스냅샷도 응답 가능하게 읽는다")
  void appendsDeliveriesToLegacyAssetIdSnapshot() {
    UUID assetId = UUID.fromString("22222222-2222-4222-8222-222222222222");
    Asset asset = asset(assetId);
    when(assetPersistencePort.findAllById(List.of(assetId))).thenReturn(List.of(asset));
    when(assetVariantDeliveryService.resolveReadyDeliveries(List.of(assetId)))
        .thenReturn(Map.of(assetId, AssetVariantDelivery.empty()));

    var results = service.appendDeliveries("[\"" + assetId + "\"]");

    assertEquals(1, results.size());
    assertEquals(assetId, results.getFirst().assetId());
    assertEquals(null, results.getFirst().source());
    assertEquals(0, results.getFirst().sortOrder());
    assertEquals("UPLOADED", results.getFirst().status());
    assertEquals(
        "https://assets.example.test/original/order-reference.jpg",
        results.getFirst().deliveryUrl());
  }

  private Asset asset(UUID assetId) {
    return Asset.create(
        assetId,
        UUID.randomUUID(),
        "order-reference.jpg",
        "image/jpeg",
        1024,
        "original/order-reference.jpg");
  }
}
