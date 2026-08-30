package io.point3.p3api.inquiry.application.submission.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderFormErrorCode;
import io.point3.p3api.inquiry.application.command.CreateOrderFormSubmissionCommand;
import io.point3.p3api.orderform.application.result.OrderFormFieldOptionResult;
import io.point3.p3api.orderform.application.result.OrderFormFieldResult;
import io.point3.p3api.orderform.domain.type.FieldType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderFormAnswerValidatorTest {

  private final OrderFormAnswerValidator validator = new OrderFormAnswerValidator();
  private final JsonNodeFactory json = JsonNodeFactory.instance;

  @Test
  @DisplayName("지원 필드 타입별 유효한 답변을 허용한다")
  void acceptsValidValuesForSupportedFieldTypes() {
    UUID textFieldId = UUID.randomUUID();
    UUID textareaFieldId = UUID.randomUUID();
    UUID imageFieldId = UUID.randomUUID();
    UUID singleSelectFieldId = UUID.randomUUID();
    UUID singleSelectWithTextFieldId = UUID.randomUUID();
    UUID multiSelectFieldId = UUID.randomUUID();

    validator.validate(
        List.of(
            field(textFieldId, FieldType.TEXT, true, 0),
            field(textareaFieldId, FieldType.TEXTAREA, true, 1),
            field(imageFieldId, FieldType.IMAGE, true, 2),
            field(singleSelectFieldId, FieldType.SINGLE_SELECT, true, 3, List.of(option("size-1"))),
            field(
                singleSelectWithTextFieldId,
                FieldType.SINGLE_SELECT_WITH_TEXT,
                true,
                4,
                List.of(option("lettering"))),
            field(
                multiSelectFieldId,
                FieldType.MULTI_SELECT,
                true,
                5,
                List.of(option("flower"), option("ribbon")))),
        List.of(
            answer(textFieldId, json.textNode("초코 케이크")),
            answer(textareaFieldId, json.textNode("문구는 작게")),
            answer(imageFieldId, json.arrayNode().add(UUID.randomUUID().toString())),
            answer(singleSelectFieldId, json.textNode("size-1")),
            answer(
                singleSelectWithTextFieldId,
                json.objectNode().put("selectedValue", "lettering").put("text", "생일 축하")),
            answer(multiSelectFieldId, json.arrayNode().add("flower").add("ribbon"))));
  }

  @Test
  @DisplayName("필수 필드의 빈 답변은 거절한다")
  void rejectsMissingRequiredField() {
    UUID fieldId = UUID.randomUUID();

    BaseException exception = assertThrows(
        BaseException.class,
        () -> validator.validate(
            List.of(field(fieldId, FieldType.TEXT, true, 0)),
            List.of(answer(fieldId, json.textNode(" ")))));

    assertEquals(OrderFormErrorCode.ORDER_FORM_REQUIRED_FIELD_MISSING, exception.getErrorCode());
  }

  @Test
  @DisplayName("양식에 없는 필드 답변은 거절한다")
  void rejectsUnknownField() {
    BaseException exception = assertThrows(
        BaseException.class,
        () -> validator.validate(
            List.of(field(UUID.randomUUID(), FieldType.TEXT, false, 0)),
            List.of(answer(UUID.randomUUID(), json.textNode("답변")))));

    assertEquals(OrderFormErrorCode.ORDER_FORM_UNKNOWN_FIELD, exception.getErrorCode());
  }

  @Test
  @DisplayName("필드 타입과 맞지 않는 답변 값은 거절한다")
  void rejectsMismatchedValueType() {
    UUID fieldId = UUID.randomUUID();

    BaseException exception = assertThrows(
        BaseException.class,
        () -> validator.validate(
            List.of(field(fieldId, FieldType.TEXT, false, 0)),
            List.of(answer(fieldId, json.numberNode(38000)))));

    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, exception.getErrorCode());
  }

  @Test
  @DisplayName("단일 선택+텍스트 답변은 selectedValue가 있어야 한다")
  void rejectsSingleSelectWithTextWithoutSelectedValue() {
    UUID fieldId = UUID.randomUUID();

    BaseException exception = assertThrows(
        BaseException.class,
        () -> validator.validate(
            List.of(field(
                fieldId,
                FieldType.SINGLE_SELECT_WITH_TEXT,
                false,
                0,
                List.of(option("lettering")))),
            List.of(answer(fieldId, json.objectNode().put("text", "Happy birthday")))));

    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, exception.getErrorCode());
  }

  @Test
  @DisplayName("복수 선택 답변은 옵션 value 배열이어야 하며 중복을 허용하지 않는다")
  void rejectsInvalidMultiSelectValues() {
    UUID fieldId = UUID.randomUUID();

    BaseException exception = assertThrows(
        BaseException.class,
        () -> validator.validate(
            List.of(field(
                fieldId,
                FieldType.MULTI_SELECT,
                false,
                0,
                List.of(option("flower"), option("ribbon")))),
            List.of(answer(fieldId, json.arrayNode().add("flower").add("flower")))));

    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, exception.getErrorCode());
  }

  @Test
  @DisplayName("이미지 답변은 UUID 문자열 배열이어야 하며 중복을 허용하지 않는다")
  void rejectsDuplicateImageAssetIds() {
    UUID fieldId = UUID.randomUUID();
    UUID assetId = UUID.randomUUID();
    JsonNode duplicateAssets = json.arrayNode().add(assetId.toString()).add(assetId.toString());

    BaseException exception = assertThrows(
        BaseException.class,
        () -> validator.validate(
            List.of(field(fieldId, FieldType.IMAGE, false, 0)),
            List.of(answer(fieldId, duplicateAssets))));

    assertEquals(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID, exception.getErrorCode());
  }

  @Test
  @DisplayName("이미지 답변은 최대 5개까지만 허용한다")
  void rejectsTooManyImageAssets() {
    UUID fieldId = UUID.randomUUID();

    BaseException exception = assertThrows(
        BaseException.class,
        () -> validator.validate(
            List.of(field(fieldId, FieldType.IMAGE, false, 0)),
            List.of(answer(
                fieldId,
                json.arrayNode()
                    .add(UUID.randomUUID().toString())
                    .add(UUID.randomUUID().toString())
                    .add(UUID.randomUUID().toString())
                    .add(UUID.randomUUID().toString())
                    .add(UUID.randomUUID().toString())
                    .add(UUID.randomUUID().toString())))));

    assertEquals(OrderFormErrorCode.ORDER_FORM_IMAGE_COUNT_EXCEEDED, exception.getErrorCode());
  }

  private OrderFormFieldResult field(
      UUID fieldId, FieldType fieldType, boolean required, int sortOrder) {
    return field(fieldId, fieldType, required, sortOrder, List.of());
  }

  private OrderFormFieldResult field(
      UUID fieldId,
      FieldType fieldType,
      boolean required,
      int sortOrder,
      List<OrderFormFieldOptionResult> options) {
    return new OrderFormFieldResult(
        fieldId,
        UUID.randomUUID(),
        "필드 " + sortOrder,
        fieldType,
        required,
        fieldType == FieldType.TEXT ? 0L : null,
        null,
        sortOrder,
        options);
  }

  private OrderFormFieldOptionResult option(String value) {
    return new OrderFormFieldOptionResult(UUID.randomUUID(), value, value, 0, true, 0);
  }

  private CreateOrderFormSubmissionCommand.FormAnswer answer(UUID fieldId, JsonNode value) {
    return new CreateOrderFormSubmissionCommand.FormAnswer(fieldId, value);
  }
}
