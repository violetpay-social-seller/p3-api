package io.point3.p3api.inquiry.application.submission.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderFormErrorCode;
import io.point3.p3api.inquiry.application.command.CreateOrderFormSubmissionCommand;
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
    UUID numberFieldId = UUID.randomUUID();
    UUID dateFieldId = UUID.randomUUID();
    UUID timeFieldId = UUID.randomUUID();
    UUID dateTimeFieldId = UUID.randomUUID();
    UUID imageFieldId = UUID.randomUUID();

    validator.validate(
        List.of(
            field(textFieldId, FieldType.TEXT, true, 0),
            field(numberFieldId, FieldType.NUMBER, true, 1),
            field(dateFieldId, FieldType.DATE, true, 2),
            field(timeFieldId, FieldType.TIME, true, 3),
            field(dateTimeFieldId, FieldType.DATETIME, true, 4),
            field(imageFieldId, FieldType.IMAGE, true, 5)),
        List.of(
            answer(textFieldId, json.textNode("초코 케이크")),
            answer(numberFieldId, json.numberNode(38000)),
            answer(dateFieldId, json.textNode("2026-08-30")),
            answer(timeFieldId, json.textNode("13:30")),
            answer(dateTimeFieldId, json.textNode("2026-08-30T13:30:00+09:00")),
            answer(imageFieldId, json.arrayNode().add(UUID.randomUUID().toString()))));
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
            List.of(field(fieldId, FieldType.NUMBER, false, 0)),
            List.of(answer(fieldId, json.textNode("38000")))));

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
    return new OrderFormFieldResult(
        fieldId, UUID.randomUUID(), "필드 " + sortOrder, fieldType, required, null, sortOrder);
  }

  private CreateOrderFormSubmissionCommand.FormAnswer answer(UUID fieldId, JsonNode value) {
    return new CreateOrderFormSubmissionCommand.FormAnswer(fieldId, value);
  }
}
