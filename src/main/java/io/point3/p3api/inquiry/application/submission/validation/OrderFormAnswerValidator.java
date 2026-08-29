package io.point3.p3api.inquiry.application.submission.validation;

import com.fasterxml.jackson.databind.JsonNode;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderFormErrorCode;
import io.point3.p3api.inquiry.application.command.CreateOrderFormSubmissionCommand;
import io.point3.p3api.orderform.application.result.OrderFormFieldResult;
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

  public void validate(
      List<OrderFormFieldResult> fields,
      List<CreateOrderFormSubmissionCommand.FormAnswer> answers) {
    Map<UUID, OrderFormFieldResult> fieldMap =
        fields.stream().collect(Collectors.toMap(OrderFormFieldResult::id, Function.identity()));

    Map<UUID, CreateOrderFormSubmissionCommand.FormAnswer> answerMap = answers.stream()
        .collect(Collectors.toMap(
            CreateOrderFormSubmissionCommand.FormAnswer::fieldId, Function.identity()));

    ensureNoUnknownFields(fieldMap, answerMap);
    ensureRequiredFieldsAnswered(fields, answerMap);

    for (CreateOrderFormSubmissionCommand.FormAnswer answer : answers) {
      OrderFormFieldResult field = fieldMap.get(answer.fieldId());
      validateValue(field, answer.value());
    }
  }

  private void ensureNoUnknownFields(
      Map<UUID, OrderFormFieldResult> fieldMap,
      Map<UUID, CreateOrderFormSubmissionCommand.FormAnswer> answerMap) {
    if (!fieldMap.keySet().containsAll(answerMap.keySet())) {
      throw new BaseException(OrderFormErrorCode.ORDER_FORM_UNKNOWN_FIELD);
    }
  }

  private void ensureRequiredFieldsAnswered(
      List<OrderFormFieldResult> fields,
      Map<UUID, CreateOrderFormSubmissionCommand.FormAnswer> answerMap) {
    for (OrderFormFieldResult field : fields) {
      if (field.required()) {
        CreateOrderFormSubmissionCommand.FormAnswer answer = answerMap.get(field.id());
        if (answer == null || isEmpty(answer.value())) {
          throw new BaseException(OrderFormErrorCode.ORDER_FORM_REQUIRED_FIELD_MISSING);
        }
      }
    }
  }

  private void validateValue(OrderFormFieldResult field, JsonNode value) {
    if (isEmpty(value)) {
      return;
    }

    switch (field.fieldType()) {
      case TEXT, TEXTAREA -> validateText(value);
      case IMAGE -> validateImage(value);
      case SINGLE_SELECT -> validateSingleSelect(field, value);
      case SINGLE_SELECT_WITH_TEXT -> validateSingleSelectWithText(field, value);
    }
  }

  private void validateSingleSelect(OrderFormFieldResult field, JsonNode value) {
    if (value == null
        || !value.isTextual()
        || field.options().stream()
            .noneMatch(option -> option.active() && option.value().equals(value.asText()))) {
      throwInvalidFieldValue();
    }
  }

  private void validateSingleSelectWithText(OrderFormFieldResult field, JsonNode value) {
    if (!value.isObject()) {
      throwInvalidFieldValue();
    }
    JsonNode selectedValue = value.get("selectedValue");
    validateSingleSelect(field, selectedValue);

    JsonNode text = value.get("text");
    if (text != null && !text.isNull() && !text.isTextual()) {
      throwInvalidFieldValue();
    }
  }

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
      throwInvalidFieldValue();
    }
  }

  private void validateImage(JsonNode value) {
    if (!value.isArray()) {
      throwInvalidFieldValue();
    }

    if (value.size() > MAX_IMAGE_COUNT) {
      throw new BaseException(OrderFormErrorCode.ORDER_FORM_IMAGE_COUNT_EXCEEDED);
    }

    HashSet<String> assetIds = new HashSet<>();
    value.forEach(node -> {
      if (!node.isTextual()) {
        throwInvalidFieldValue();
      }

      String assetId = node.asText();
      try {
        UUID.fromString(assetId);
      } catch (IllegalArgumentException e) {
        throwInvalidFieldValue();
      }

      if (!assetIds.add(assetId)) {
        throwInvalidFieldValue();
      }
    });
  }

  private void throwInvalidFieldValue() {
    throw new BaseException(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID);
  }
}
