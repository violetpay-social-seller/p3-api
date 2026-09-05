package io.point3.p3api.inquiry.controller.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
