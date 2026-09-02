package io.point3.p3api.inquiry.application.submission.snapshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.point3.p3api.inquiry.application.command.CreateOrderFormSubmissionCommand;
import io.point3.p3api.orderform.application.result.OrderFormOptionGroupResult;
import io.point3.p3api.orderform.application.result.OrderFormOptionResult;
import io.point3.p3api.orderform.domain.type.OptionInputType;
import io.point3.p3api.orderform.domain.type.SelectionType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderFormAnswerSnapshotFactoryTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final JsonNodeFactory json = objectMapper.getNodeFactory();
  private final OrderFormAnswerSnapshotFactory factory =
      new OrderFormAnswerSnapshotFactory(objectMapper);

  @Test
  @DisplayName("주문서 답변 스냅샷은 서버 옵션그룹/옵션 정의의 라벨과 가격을 저장한다")
  void snapshotsServerOptionGroupAndOptionPrices() throws Exception {
    UUID textGroupId = UUID.randomUUID();
    UUID selectGroupId = UUID.randomUUID();
    UUID multiGroupId = UUID.randomUUID();
    UUID textOptionId = UUID.randomUUID();
    UUID optionId = UUID.randomUUID();
    UUID flowerOptionId = UUID.randomUUID();
    UUID ribbonOptionId = UUID.randomUUID();

    String snapshot = factory.create(
        List.of(
            new OrderFormOptionGroupResult(
                textGroupId,
                UUID.randomUUID(),
                "메뉴명",
                SelectionType.SINGLE,
                true,
                0,
                List.of(new OrderFormOptionResult(
                    textOptionId, "메뉴명", "menu", OptionInputType.TEXT, 1000L, null, true, 0))),
            new OrderFormOptionGroupResult(
                selectGroupId,
                UUID.randomUUID(),
                "문구",
                SelectionType.SINGLE,
                false,
                1,
                List.of(new OrderFormOptionResult(
                    optionId,
                    "문구 추가",
                    "lettering",
                    OptionInputType.SELECT_WITH_TEXT,
                    3000L,
                    null,
                    true,
                    0))),
            new OrderFormOptionGroupResult(
                multiGroupId,
                UUID.randomUUID(),
                "케이크 디자인",
                SelectionType.MULTI,
                false,
                2,
                List.of(
                    new OrderFormOptionResult(
                        flowerOptionId,
                        "플라워",
                        "flower",
                        OptionInputType.SELECT,
                        5000L,
                        null,
                        true,
                        0),
                    new OrderFormOptionResult(
                        ribbonOptionId,
                        "리본",
                        "ribbon",
                        OptionInputType.SELECT,
                        2000L,
                        null,
                        true,
                        1)))),
        List.of(
            new CreateOrderFormSubmissionCommand.FormAnswer(
                textGroupId, selections(selection("menu").put("text", "초코 케이크"))),
            new CreateOrderFormSubmissionCommand.FormAnswer(
                selectGroupId, selections(selection("lettering").put("text", "Happy birthday"))),
            new CreateOrderFormSubmissionCommand.FormAnswer(
                multiGroupId, selections(selection("flower"), selection("ribbon")))));

    JsonNode answers = objectMapper.readTree(snapshot);
    assertEquals("메뉴명", answers.get(0).get("label").asText());
    assertEquals("SINGLE", answers.get(0).get("selectionType").asText());
    assertEquals(
        "TEXT", answers.get(0).get("selectedOptions").get(0).get("inputType").asText());
    assertEquals(1000, answers.get(0).get("selectedOptions").get(0).get("price").asLong());
    assertEquals(
        "초코 케이크", answers.get(0).get("selectedOptions").get(0).get("text").asText());
    assertEquals("문구", answers.get(1).get("label").asText());
    assertEquals(
        "SELECT_WITH_TEXT",
        answers.get(1).get("selectedOptions").get(0).get("inputType").asText());
    assertEquals(
        "문구 추가", answers.get(1).get("selectedOptions").get(0).get("label").asText());
    assertEquals(
        "lettering", answers.get(1).get("selectedOptions").get(0).get("value").asText());
    assertEquals(3000, answers.get(1).get("selectedOptions").get(0).get("price").asLong());
    assertEquals("MULTI", answers.get(2).get("selectionType").asText());
    assertEquals(
        "플라워", answers.get(2).get("selectedOptions").get(0).get("label").asText());
    assertEquals("리본", answers.get(2).get("selectedOptions").get(1).get("label").asText());
    assertEquals(
        7000,
        answers.get(2).get("selectedOptions").get(0).get("price").asLong()
            + answers.get(2).get("selectedOptions").get(1).get("price").asLong());
  }

  @Test
  @DisplayName("주문서 답변 스냅샷은 가격 문구를 숫자 가격과 구분해 저장한다")
  void snapshotsPriceLabel() throws Exception {
    UUID optionGroupId = UUID.randomUUID();
    String snapshot = factory.create(
        List.of(new OrderFormOptionGroupResult(
            optionGroupId,
            UUID.randomUUID(),
            "맞춤 크기",
            SelectionType.SINGLE,
            true,
            0,
            List.of(new OrderFormOptionResult(
                UUID.randomUUID(),
                "맞춤 크기",
                "custom-size",
                OptionInputType.SELECT,
                null,
                "문의필요",
                null,
                true,
                0)))),
        List.of(new CreateOrderFormSubmissionCommand.FormAnswer(
            optionGroupId, selections(selection("custom-size")))));

    JsonNode option =
        objectMapper.readTree(snapshot).get(0).get("selectedOptions").get(0);
    assertTrue(option.get("price").isNull());
    assertEquals("문의필요", option.get("priceLabel").asText());
  }

  private com.fasterxml.jackson.databind.node.ObjectNode selection(String optionValue) {
    return json.objectNode().put("optionValue", optionValue);
  }

  private com.fasterxml.jackson.databind.node.ArrayNode selections(JsonNode... selectedOptions) {
    com.fasterxml.jackson.databind.node.ArrayNode selections = json.arrayNode();
    for (JsonNode selectedOption : selectedOptions) {
      selections.add(selectedOption);
    }
    return selections;
  }
}
