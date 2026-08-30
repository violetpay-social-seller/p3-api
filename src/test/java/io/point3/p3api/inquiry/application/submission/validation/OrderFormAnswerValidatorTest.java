package io.point3.p3api.inquiry.application.submission.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderFormErrorCode;
import io.point3.p3api.inquiry.application.command.CreateOrderFormSubmissionCommand;
import io.point3.p3api.orderform.application.result.OrderFormOptionGroupResult;
import io.point3.p3api.orderform.application.result.OrderFormOptionResult;
import io.point3.p3api.orderform.domain.type.OptionInputType;
import io.point3.p3api.orderform.domain.type.SelectionType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderFormAnswerValidatorTest {

  private final OrderFormAnswerValidator validator = new OrderFormAnswerValidator();
  private final JsonNodeFactory json = JsonNodeFactory.instance;

  @Test
  @DisplayName("지원 옵션 입력 타입별 유효한 답변을 허용한다")
  void acceptsValidValuesForSupportedOptionInputTypes() {
    UUID textGroupId = UUID.randomUUID();
    UUID textareaGroupId = UUID.randomUUID();
    UUID imageGroupId = UUID.randomUUID();
    UUID selectGroupId = UUID.randomUUID();
    UUID selectWithTextGroupId = UUID.randomUUID();
    UUID multiGroupId = UUID.randomUUID();
    UUID imageAssetId = UUID.randomUUID();

    validator.validate(
        List.of(
            group(textGroupId, SelectionType.SINGLE, true, 0, option("menu", OptionInputType.TEXT)),
            group(
                textareaGroupId,
                SelectionType.SINGLE,
                true,
                1,
                option("memo", OptionInputType.TEXTAREA)),
            group(
                imageGroupId,
                SelectionType.SINGLE,
                true,
                2,
                option("reference", OptionInputType.IMAGE)),
            group(selectGroupId, SelectionType.SINGLE, true, 3, option("size-1")),
            group(
                selectWithTextGroupId,
                SelectionType.SINGLE,
                true,
                4,
                option("lettering", OptionInputType.SELECT_WITH_TEXT)),
            group(multiGroupId, SelectionType.MULTI, true, 5, option("flower"), option("ribbon"))),
        List.of(
            answer(textGroupId, selections(selection("menu").put("text", "초코 케이크"))),
            answer(textareaGroupId, selections(selection("memo").put("text", "문구는 작게"))),
            answer(
                imageGroupId,
                selections(selection("reference")
                    .set("assetIds", json.arrayNode().add(imageAssetId.toString())))),
            answer(selectGroupId, selections(selection("size-1"))),
            answer(selectWithTextGroupId, selections(selection("lettering").put("text", "생일 축하"))),
            answer(multiGroupId, selections(selection("flower"), selection("ribbon")))));
  }

  @Test
  @DisplayName("필수 옵션그룹의 빈 답변은 거절한다")
  void rejectsMissingRequiredField() {
    UUID optionGroupId = UUID.randomUUID();

    BaseException exception = assertThrows(
        BaseException.class,
        () -> validator.validate(
            List.of(group(
                optionGroupId,
                SelectionType.SINGLE,
                true,
                0,
                option("menu", OptionInputType.TEXT))),
            List.of(answer(optionGroupId, json.arrayNode()))));

    assertEquals(OrderFormErrorCode.ORDER_FORM_REQUIRED_FIELD_MISSING, exception.getErrorCode());
  }

  @Test
  @DisplayName("양식에 없는 옵션그룹 답변은 거절한다")
  void rejectsUnknownField() {
    BaseException exception = assertThrows(
        BaseException.class,
        () -> validator.validate(
            List.of(group(UUID.randomUUID(), SelectionType.SINGLE, false, 0, option("menu"))),
            List.of(answer(UUID.randomUUID(), selections(selection("menu"))))));

    assertEquals(OrderFormErrorCode.ORDER_FORM_UNKNOWN_FIELD, exception.getErrorCode());
  }

  @Test
  @DisplayName("옵션그룹 답변은 선택 객체 배열이어야 한다")
  void rejectsMismatchedValueType() {
    UUID optionGroupId = UUID.randomUUID();

    BaseException exception = assertThrows(
        BaseException.class,
        () -> validator.validate(
            List.of(group(optionGroupId, SelectionType.SINGLE, false, 0, option("menu"))),
            List.of(answer(optionGroupId, json.textNode("menu")))));

    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, exception.getErrorCode());
  }

  @Test
  @DisplayName("단일 선택 옵션그룹은 하나의 선택만 허용한다")
  void rejectsMultipleValuesForSingleSelectionGroup() {
    UUID optionGroupId = UUID.randomUUID();

    BaseException exception = assertThrows(
        BaseException.class,
        () -> validator.validate(
            List.of(group(
                optionGroupId, SelectionType.SINGLE, false, 0, option("flower"), option("ribbon"))),
            List.of(answer(optionGroupId, selections(selection("flower"), selection("ribbon"))))));

    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, exception.getErrorCode());
  }

  @Test
  @DisplayName("텍스트 입력 옵션은 text가 있어야 한다")
  void rejectsTextOptionWithoutText() {
    UUID optionGroupId = UUID.randomUUID();

    BaseException exception = assertThrows(
        BaseException.class,
        () -> validator.validate(
            List.of(group(
                optionGroupId,
                SelectionType.SINGLE,
                false,
                0,
                option("lettering", OptionInputType.SELECT_WITH_TEXT))),
            List.of(answer(optionGroupId, selections(selection("lettering"))))));

    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, exception.getErrorCode());
  }

  @Test
  @DisplayName("복수 선택 답변은 중복 옵션 value를 허용하지 않는다")
  void rejectsInvalidMultiSelectValues() {
    UUID optionGroupId = UUID.randomUUID();

    BaseException exception = assertThrows(
        BaseException.class,
        () -> validator.validate(
            List.of(group(
                optionGroupId, SelectionType.MULTI, false, 0, option("flower"), option("ribbon"))),
            List.of(answer(optionGroupId, selections(selection("flower"), selection("flower"))))));

    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, exception.getErrorCode());
  }

  @Test
  @DisplayName("이미지 입력 옵션은 UUID 문자열 배열이어야 하며 중복을 허용하지 않는다")
  void rejectsDuplicateImageAssetIds() {
    UUID optionGroupId = UUID.randomUUID();
    UUID assetId = UUID.randomUUID();
    JsonNode selected = selection("reference")
        .set("assetIds", json.arrayNode().add(assetId.toString()).add(assetId.toString()));

    BaseException exception = assertThrows(
        BaseException.class,
        () -> validator.validate(
            List.of(group(
                optionGroupId,
                SelectionType.SINGLE,
                false,
                0,
                option("reference", OptionInputType.IMAGE))),
            List.of(answer(optionGroupId, selections(selected)))));

    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, exception.getErrorCode());
  }

  @Test
  @DisplayName("이미지 입력 옵션은 최대 5개까지만 허용한다")
  void rejectsTooManyImageAssets() {
    UUID optionGroupId = UUID.randomUUID();
    ArrayNode assetIds = json.arrayNode()
        .add(UUID.randomUUID().toString())
        .add(UUID.randomUUID().toString())
        .add(UUID.randomUUID().toString())
        .add(UUID.randomUUID().toString())
        .add(UUID.randomUUID().toString())
        .add(UUID.randomUUID().toString());

    BaseException exception = assertThrows(
        BaseException.class,
        () -> validator.validate(
            List.of(group(
                optionGroupId,
                SelectionType.SINGLE,
                false,
                0,
                option("reference", OptionInputType.IMAGE))),
            List.of(answer(
                optionGroupId, selections(selection("reference").set("assetIds", assetIds))))));

    assertEquals(OrderFormErrorCode.ORDER_FORM_IMAGE_COUNT_EXCEEDED, exception.getErrorCode());
  }

  private OrderFormOptionGroupResult group(
      UUID optionGroupId,
      SelectionType selectionType,
      boolean required,
      int sortOrder,
      OrderFormOptionResult... options) {
    return new OrderFormOptionGroupResult(
        optionGroupId,
        UUID.randomUUID(),
        "옵션그룹 " + sortOrder,
        selectionType,
        required,
        sortOrder,
        List.of(options));
  }

  private OrderFormOptionResult option(String value) {
    return option(value, OptionInputType.SELECT);
  }

  private OrderFormOptionResult option(String value, OptionInputType inputType) {
    Long price =
        switch (inputType) {
          case SELECT, SELECT_WITH_TEXT, TEXT -> 0L;
          case TEXTAREA, IMAGE -> null;
        };
    return new OrderFormOptionResult(
        UUID.randomUUID(), value, value, inputType, price, null, true, 0);
  }

  private com.fasterxml.jackson.databind.node.ObjectNode selection(String optionValue) {
    return json.objectNode().put("optionValue", optionValue);
  }

  private ArrayNode selections(JsonNode... selectedOptions) {
    ArrayNode selections = json.arrayNode();
    for (JsonNode selectedOption : selectedOptions) {
      selections.add(selectedOption);
    }
    return selections;
  }

  private CreateOrderFormSubmissionCommand.FormAnswer answer(UUID optionGroupId, JsonNode value) {
    return new CreateOrderFormSubmissionCommand.FormAnswer(optionGroupId, value);
  }
}
