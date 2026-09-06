package io.point3.p3api.inquiry.controller.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.point3.p3api.inquiry.domain.type.OrderFormReferenceAssetSource;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CreateOrderFormDraftRequestTest {

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  @Test
  @DisplayName("startReferenceAsset 미제공과 명시적 null을 구분한다")
  void distinguishesMissingAndNullStartReferenceAsset() throws Exception {
    CreateOrderFormDraftRequest missing =
        objectMapper.readValue(baseJson(""), CreateOrderFormDraftRequest.class);
    CreateOrderFormDraftRequest explicitNull = objectMapper.readValue(
        baseJson(",\"startReferenceAsset\":null"), CreateOrderFormDraftRequest.class);

    assertFalse(missing.startReferenceAssetProvided());
    assertNull(missing.startReferenceAsset());
    assertTrue(explicitNull.startReferenceAssetProvided());
    assertNull(explicitNull.startReferenceAsset());
  }

  @Test
  @DisplayName("startReferenceAsset은 단일 객체로 읽는다")
  void readsSingleStartReferenceAsset() throws Exception {
    UUID assetId = UUID.randomUUID();

    CreateOrderFormDraftRequest request = objectMapper.readValue(
        baseJson(",\"startReferenceAsset\":{\"assetId\":\""
            + assetId
            + "\",\"source\":\"STORE_GALLERY\"}"),
        CreateOrderFormDraftRequest.class);

    assertTrue(request.startReferenceAssetProvided());
    assertEquals(assetId, request.startReferenceAsset().assetId());
    assertEquals(
        OrderFormReferenceAssetSource.STORE_GALLERY,
        request.startReferenceAsset().source());
  }

  @Test
  @DisplayName("Jackson 3 HTTP 파싱에서도 내부 제공 여부 필드를 요청값으로 요구하지 않는다")
  void readsDraftRequestWithJackson3() throws Exception {
    tools.jackson.databind.ObjectMapper jackson3Mapper =
        tools.jackson.databind.json.JsonMapper.builderWithJackson2Defaults().build();

    CreateOrderFormDraftRequest request =
        jackson3Mapper.readValue(productionPayload(), CreateOrderFormDraftRequest.class);

    assertEquals(
        UUID.fromString("22222222-2222-4222-8222-000000000001"), request.orderFormTemplateId());
    assertTrue(request.startReferenceAssetProvided());
    assertEquals(
        UUID.fromString("719fbfe0-5c31-4a7c-bdbb-d6c1a3940867"),
        request.startReferenceAsset().assetId());
    assertEquals(
        OrderFormReferenceAssetSource.USER_UPLOAD, request.startReferenceAsset().source());
    JsonNode answerValue =
        objectMapper.valueToTree(request.formAnswers().getFirst().value());
    assertTrue(answerValue.isArray());
    assertEquals("size_1", answerValue.get(0).get("optionValue").asText());
  }

  private String baseJson(String startReferenceAssetJson) {
    return """
        {
          "orderFormTemplateId": "11111111-1111-1111-1111-111111111111",
          "pickupDate": "2026-09-01",
          "pickupTime": "15:00",
          "noticeAgreed": true,
          "cancellationRefundAgreed": true,
          "formAnswers": [
            {
              "optionGroupId": "22222222-2222-2222-2222-222222222222",
              "value": [{"optionKey": "menu", "text": "cake"}]
            }
          ]""" + startReferenceAssetJson + """
        }
        """;
  }

  private String productionPayload() {
    return """
        {
          "cancellationRefundAgreed": true,
          "formAnswers": [
            {
              "optionGroupId": "22222222-2222-4222-8222-000000001101",
              "value": [{"optionValue": "size_1"}]
            },
            {
              "optionGroupId": "22222222-2222-4222-8222-000000002101",
              "value": [{"optionValue": "round"}]
            },
            {
              "optionGroupId": "22222222-2222-4222-8222-000000003101",
              "value": [{"optionValue": "choco_cream"}]
            },
            {
              "optionGroupId": "22222222-2222-4222-8222-000000005101",
              "value": [{"optionValue": "basic_box"}]
            }
          ],
          "noticeAgreed": true,
          "orderFormTemplateId": "22222222-2222-4222-8222-000000000001",
          "pickupDate": "2026-09-08",
          "pickupTime": "15:00:00",
          "startReferenceAsset": {
            "assetId": "719fbfe0-5c31-4a7c-bdbb-d6c1a3940867",
            "source": "USER_UPLOAD"
          },
          "startReferenceAssets": [
            {
              "assetId": "719fbfe0-5c31-4a7c-bdbb-d6c1a3940867",
              "source": "USER_UPLOAD",
              "sortOrder": 0
            }
          ]
        }
        """;
  }
}
