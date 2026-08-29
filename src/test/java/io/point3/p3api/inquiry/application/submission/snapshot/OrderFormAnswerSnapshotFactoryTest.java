package io.point3.p3api.inquiry.application.submission.snapshot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.inquiry.application.command.CreateOrderFormSubmissionCommand;
import io.point3.p3api.orderform.application.result.OrderFormFieldOptionResult;
import io.point3.p3api.orderform.application.result.OrderFormFieldResult;
import io.point3.p3api.orderform.domain.type.FieldType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderFormAnswerSnapshotFactoryTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final OrderFormAnswerSnapshotFactory factory =
      new OrderFormAnswerSnapshotFactory(objectMapper);

  @Test
  @DisplayName("주문서 답변 스냅샷은 서버 필드/선택지 정의의 라벨과 가격을 저장한다")
  void snapshotsServerFieldAndOptionPrices() throws Exception {
    UUID textFieldId = UUID.randomUUID();
    UUID selectFieldId = UUID.randomUUID();
    UUID optionId = UUID.randomUUID();

    String snapshot = factory.create(
        List.of(
            new OrderFormFieldResult(
                textFieldId, UUID.randomUUID(), "메뉴명", FieldType.TEXT, true, 1000L, null, 0),
            new OrderFormFieldResult(
                selectFieldId,
                UUID.randomUUID(),
                "문구",
                FieldType.SINGLE_SELECT_WITH_TEXT,
                false,
                null,
                null,
                1,
                List.of(new OrderFormFieldOptionResult(
                    optionId, "문구 추가", "lettering", 3000, true, 0)))),
        List.of(
            new CreateOrderFormSubmissionCommand.FormAnswer(
                textFieldId, objectMapper.getNodeFactory().textNode("초코 케이크")),
            new CreateOrderFormSubmissionCommand.FormAnswer(
                selectFieldId,
                objectMapper
                    .getNodeFactory()
                    .objectNode()
                    .put("selectedValue", "lettering")
                    .put("text", "Happy birthday"))));

    JsonNode answers = objectMapper.readTree(snapshot);
    assertEquals("메뉴명", answers.get(0).get("label").asText());
    assertEquals("TEXT", answers.get(0).get("fieldType").asText());
    assertEquals(1000, answers.get(0).get("price").asLong());
    assertEquals("문구", answers.get(1).get("label").asText());
    assertEquals("SINGLE_SELECT_WITH_TEXT", answers.get(1).get("fieldType").asText());
    assertEquals(true, answers.get(1).get("price").isNull());
    assertEquals(
        "문구 추가", answers.get(1).get("selectedOptions").get(0).get("label").asText());
    assertEquals(
        "lettering", answers.get(1).get("selectedOptions").get(0).get("value").asText());
    assertEquals(3000, answers.get(1).get("selectedOptions").get(0).get("price").asLong());
  }
}
