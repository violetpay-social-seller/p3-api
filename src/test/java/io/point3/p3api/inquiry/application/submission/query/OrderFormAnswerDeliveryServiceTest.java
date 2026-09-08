package io.point3.p3api.inquiry.application.submission.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.inquiry.application.submission.result.OrderFormAssetDelivery;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class OrderFormAnswerDeliveryServiceTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final OrderFormAssetDeliveryResolver deliveryResolver =
      Mockito.mock(OrderFormAssetDeliveryResolver.class);
  private final OrderFormAnswerDeliveryService service =
      new OrderFormAnswerDeliveryService(objectMapper, deliveryResolver);

  @Test
  void appendsImageAssetDeliveries() throws Exception {
    UUID assetId = UUID.fromString("11111111-1111-4111-8111-111111111111");
    String answers = """
        [
          {
            "label": "사진 첨부",
            "value": [
              {
                "optionValue": "reference",
                "assetIds": ["11111111-1111-4111-8111-111111111111"]
              }
            ],
            "selectedOptions": [
              {
                "value": "reference",
                "assetIds": ["11111111-1111-4111-8111-111111111111"]
              }
            ]
          }
        ]
        """;
    when(deliveryResolver.resolve(List.of(assetId)))
        .thenReturn(Map.of(
            assetId,
            new OrderFormAssetDelivery(
                "READY",
                "https://assets.example.test/processed/reference_640.webp",
                List.of(new OrderFormAssetDelivery.Variant(
                    "MEDIUM",
                    "https://assets.example.test/processed/reference_640.webp",
                    640,
                    480)))));

    JsonNode answer = objectMapper.readTree(service.appendImageDeliveries(answers)).get(0);

    assertAsset(answer.get("value").get(0));
    assertAsset(answer.get("selectedOptions").get(0));
  }

  @Test
  void ignoresAnswersWithoutAssetIds() {
    String answers = "[{\"label\":\"메뉴명\",\"value\":\"초코 케이크\"}]";

    assertEquals(answers, service.appendImageDeliveries(answers));
  }

  @Test
  void rejectsInvalidJson() {
    org.junit.jupiter.api.Assertions.assertThrows(
        BaseException.class, () -> service.appendImageDeliveries("{"));
  }

  private void assertAsset(JsonNode selectedOption) {
    JsonNode asset = selectedOption.get("assets").get(0);
    assertEquals("11111111-1111-4111-8111-111111111111", asset.get("assetId").asText());
    assertEquals("READY", asset.get("status").asText());
    assertEquals(
        "https://assets.example.test/processed/reference_640.webp",
        asset.get("deliveryUrl").asText());
    assertEquals("MEDIUM", asset.get("variants").get(0).get("type").asText());
  }
}
