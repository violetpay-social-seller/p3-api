package io.point3.p3api.inquiry.application.submission.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.assetvariant.application.AssetVariantDeliveryService;
import io.point3.p3api.assetvariant.application.result.AssetVariantDelivery;
import io.point3.p3api.inquiry.domain.type.OrderFormReferenceAssetSource;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class OrderFormReferenceAssetDeliveryServiceTest {

  private final AssetVariantDeliveryService assetVariantDeliveryService =
      Mockito.mock(AssetVariantDeliveryService.class);
  private final OrderFormReferenceAssetDeliveryService service =
      new OrderFormReferenceAssetDeliveryService(new ObjectMapper(), assetVariantDeliveryService);

  @Test
  @DisplayName("주문서 참고 이미지 스냅샷에 delivery URL과 variants를 붙인다")
  void appendsDeliveries() {
    UUID assetId = UUID.fromString("11111111-1111-4111-8111-111111111111");
    String snapshot = """
        [
          {
            "assetId": "11111111-1111-4111-8111-111111111111",
            "source": "STORE_GALLERY",
            "sortOrder": 0
          }
        ]
        """;
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
    assertEquals(0, results.getFirst().sortOrder());
    assertEquals(
        "https://assets.example.test/processed/cake_640.webp",
        results.getFirst().deliveryUrl());
    assertEquals(1, results.getFirst().variants().size());
    assertEquals("MEDIUM", results.getFirst().variants().getFirst().type());
  }

  @Test
  @DisplayName("참고 이미지 스냅샷이 없으면 빈 목록을 반환한다")
  void returnsEmptyResult() {
    assertTrue(service.appendDeliveries(null).isEmpty());
    assertTrue(service.appendDeliveries("").isEmpty());
  }
}
