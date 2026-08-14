package io.point3.p3api.inquiry.application.submit;

import com.fasterxml.jackson.databind.JsonNode;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderFormErrorCode;
import io.point3.p3api.orderform.application.result.OrderFormFieldResult;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class OrderFormAnswerValidator {

  private static final int MAX_IMAGE_COUNT = 5;

  /**
   * 전체 검증 진입점 - 필드 정의와 제출 답변 매핑
   */
  public void validate(
      List<OrderFormFieldResult> fields, List<SubmitPreOrderCommand.FormAnswer> answers) {
    Map<UUID, OrderFormFieldResult> fieldMap =
        fields.stream().collect(Collectors.toMap(OrderFormFieldResult::id, Function.identity()));

    Map<UUID, SubmitPreOrderCommand.FormAnswer> answerMap = answers.stream()
        .collect(Collectors.toMap(SubmitPreOrderCommand.FormAnswer::fieldId, Function.identity()));

    ensureNoUnknownFields(fieldMap, answerMap);
    ensureRequiredFieldsAnswered(fields, answerMap);

    for (SubmitPreOrderCommand.FormAnswer answer : answers) {
      OrderFormFieldResult field = fieldMap.get(answer.fieldId());
      validateValue(field, answer.value());
    }
  }

  /**
   * 제출된 fieldId가 현재 주문서 양식에 실제로 존재하는 필드인지 확인
   */
  private void ensureNoUnknownFields(
      Map<UUID, OrderFormFieldResult> fieldMap,
      Map<UUID, SubmitPreOrderCommand.FormAnswer> answerMap) {
    if (!fieldMap.keySet().containsAll(answerMap.keySet())) {
      throw new BaseException(OrderFormErrorCode.ORDER_FORM_INVALID_FIELD);
    }
  }

  /**
   * 답변 필수인 필드가 정확히 제출되었는지
   */
  private void ensureRequiredFieldsAnswered(
      List<OrderFormFieldResult> fields, Map<UUID, SubmitPreOrderCommand.FormAnswer> answerMap) {
    for (OrderFormFieldResult field : fields) {
      if (field.required()) {
        SubmitPreOrderCommand.FormAnswer answer = answerMap.get(field.id());
        if (answer == null || isEmpty(answer.value())) {
          throw new BaseException(OrderFormErrorCode.ORDER_FORM_INVALID_FIELD);
        }
      }
    }
  }

  /**
   * 필드 타입에 따라서 실제 값 검증 메서드로 분기 없으면 Optional필드(필수x)로 보고 통과
   */
  private void validateValue(OrderFormFieldResult field, JsonNode value) {
    if (isEmpty(value)) {
      return;
    }

    switch (field.fieldType()) {
      case TEXT, TEXTAREA -> validateText(value);
      case NUMBER -> validateNumber(value);
      case DATE -> validateDate(value);
      case TIME -> validateTime(value);
      case DATETIME -> validateDateTime(value);
      case IMAGE -> validateImage(value);
    }
  }

  // ================타입에 따른 실제 값 검증============================

  private boolean isEmpty(JsonNode value) {
    if (value == null || value.isNull()) {
      return true;
    }

    if (value.isTextual()) {
      return value.asText().isBlank();
    }

    if (value.isArray()) {
      return value.isEmpty();
    }

    return false;
  }

  private void validateText(JsonNode value) {
    if (!value.isTextual()) {
      throw new BaseException(OrderFormErrorCode.ORDER_FORM_INVALID_FIELD);
    }
  }

  private void validateNumber(JsonNode value) {
    if (!value.isNumber()) {
      throw new BaseException(OrderFormErrorCode.ORDER_FORM_INVALID_FIELD);
    }
  }

  private void validateDate(JsonNode value) {
    if (!value.isTextual()) {
      throw new BaseException(OrderFormErrorCode.ORDER_FORM_INVALID_FIELD);
    }

    try {
      LocalDate.parse(value.asText());
    } catch (DateTimeParseException e) {
      throw new BaseException(OrderFormErrorCode.ORDER_FORM_INVALID_FIELD);
    }
  }

  private void validateTime(JsonNode value) {
    if (!value.isTextual()) {
      throw new BaseException(OrderFormErrorCode.ORDER_FORM_INVALID_FIELD);
    }

    try {
      LocalTime.parse(value.asText());
    } catch (DateTimeParseException e) {
      throw new BaseException(OrderFormErrorCode.ORDER_FORM_INVALID_FIELD);
    }
  }

  private void validateDateTime(JsonNode value) {
    if (!value.isTextual()) {
      throw new BaseException(OrderFormErrorCode.ORDER_FORM_INVALID_FIELD);
    }

    try {
      OffsetDateTime.parse(value.asText());
    } catch (DateTimeParseException e) {
      throw new BaseException(OrderFormErrorCode.ORDER_FORM_INVALID_FIELD);
    }
  }

  private void validateImage(JsonNode value) {
    if (!value.isArray()) {
      throw new BaseException(OrderFormErrorCode.ORDER_FORM_INVALID_FIELD);
    }

    if (value.size() > MAX_IMAGE_COUNT) {
      throw new BaseException(OrderFormErrorCode.ORDER_FORM_INVALID_FIELD);
    }

    HashSet<String> assetIds = new HashSet<>();
    value.forEach(node -> {
      if (!node.isTextual()) {
        throw new BaseException(OrderFormErrorCode.ORDER_FORM_INVALID_FIELD);
      }

      String assetId = node.asText();
      try {
        UUID.fromString(assetId);
      } catch (IllegalArgumentException e) {
        throw new BaseException(OrderFormErrorCode.ORDER_FORM_INVALID_FIELD);
      }

      if (!assetIds.add(assetId)) {
        throw new BaseException(OrderFormErrorCode.ORDER_FORM_INVALID_FIELD);
      }
    });
  }
}
