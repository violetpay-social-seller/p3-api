package io.point3.p3api.inquiry.application.submission.validation;

import com.fasterxml.jackson.databind.JsonNode;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderFormErrorCode;
import io.point3.p3api.inquiry.application.command.CreateOrderFormSubmissionCommand;
import io.point3.p3api.orderform.application.result.OrderFormOptionGroupResult;
import io.point3.p3api.orderform.application.result.OrderFormOptionResult;
import io.point3.p3api.orderform.domain.type.OptionInputType;
import io.point3.p3api.orderform.domain.type.SelectionType;
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
      List<OrderFormOptionGroupResult> optionGroups,
      List<CreateOrderFormSubmissionCommand.FormAnswer> answers) {
    Map<UUID, OrderFormOptionGroupResult> optionGroupMap = optionGroups.stream()
        .collect(Collectors.toMap(OrderFormOptionGroupResult::id, Function.identity()));

    Map<UUID, CreateOrderFormSubmissionCommand.FormAnswer> answerMap = answers.stream()
        .collect(Collectors.toMap(
            CreateOrderFormSubmissionCommand.FormAnswer::optionGroupId, Function.identity()));

    ensureNoUnknownOptionGroups(optionGroupMap, answerMap);
    ensureRequiredOptionGroupsAnswered(optionGroups, answerMap);

    for (CreateOrderFormSubmissionCommand.FormAnswer answer : answers) {
      OrderFormOptionGroupResult optionGroup = optionGroupMap.get(answer.optionGroupId());
      validateValue(optionGroup, answer.value());
    }
  }

  private void ensureNoUnknownOptionGroups(
      Map<UUID, OrderFormOptionGroupResult> optionGroupMap,
      Map<UUID, CreateOrderFormSubmissionCommand.FormAnswer> answerMap) {
    if (!optionGroupMap.keySet().containsAll(answerMap.keySet())) {
      throw new BaseException(OrderFormErrorCode.ORDER_FORM_UNKNOWN_FIELD);
    }
  }

  private void ensureRequiredOptionGroupsAnswered(
      List<OrderFormOptionGroupResult> optionGroups,
      Map<UUID, CreateOrderFormSubmissionCommand.FormAnswer> answerMap) {
    for (OrderFormOptionGroupResult optionGroup : optionGroups) {
      if (optionGroup.required()) {
        CreateOrderFormSubmissionCommand.FormAnswer answer = answerMap.get(optionGroup.id());
        if (answer == null || isEmpty(answer.value())) {
          throw new BaseException(OrderFormErrorCode.ORDER_FORM_REQUIRED_FIELD_MISSING);
        }
      }
    }
  }

  private void validateValue(OrderFormOptionGroupResult optionGroup, JsonNode value) {
    if (isEmpty(value)) {
      return;
    }

    if (!value.isArray()) {
      throwInvalidFieldValue();
    }

    if (optionGroup.selectionType() == SelectionType.SINGLE && value.size() != 1) {
      throwInvalidFieldValue();
    }

    HashSet<String> selectedValues = new HashSet<>();
    Map<String, OrderFormOptionResult> optionMap = optionGroup.options().stream()
        .collect(Collectors.toMap(OrderFormOptionResult::value, Function.identity()));

    for (JsonNode selected : value) {
      if (!selected.isObject()) {
        throwInvalidFieldValue();
      }

      JsonNode optionValueNode = selected.get("optionValue");
      if (optionValueNode == null || !optionValueNode.isTextual()) {
        throwInvalidFieldValue();
      }

      String optionValue = optionValueNode.asText();
      if (optionValue.isBlank() || !selectedValues.add(optionValue)) {
        throwInvalidFieldValue();
      }

      OrderFormOptionResult option = optionMap.get(optionValue);
      if (option == null || !option.active()) {
        throwInvalidFieldValue();
      }

      validateSelectedOption(option.inputType(), selected);
    }
  }

  private void validateSelectedOption(OptionInputType inputType, JsonNode selected) {
    switch (inputType) {
      case SELECT -> validateSelect(selected);
      case SELECT_WITH_TEXT, TEXT, TEXTAREA -> validateText(selected);
      case IMAGE -> validateImage(selected);
    }
  }

  private void validateSelect(JsonNode selected) {
    if (hasPresent(selected.get("text")) || hasPresent(selected.get("assetIds"))) {
      throwInvalidFieldValue();
    }
  }

  private void validateText(JsonNode selected) {
    JsonNode text = selected.get("text");
    if (text == null || !text.isTextual() || text.asText().isBlank()) {
      throwInvalidFieldValue();
    }
    if (hasPresent(selected.get("assetIds"))) {
      throwInvalidFieldValue();
    }
  }

  private void validateImage(JsonNode selected) {
    JsonNode assetIds = selected.get("assetIds");
    if (assetIds == null || !assetIds.isArray() || assetIds.isEmpty()) {
      throwInvalidFieldValue();
    }

    if (assetIds.size() > MAX_IMAGE_COUNT) {
      throw new BaseException(OrderFormErrorCode.ORDER_FORM_IMAGE_COUNT_EXCEEDED);
    }

    if (hasPresent(selected.get("text"))) {
      throwInvalidFieldValue();
    }

    HashSet<String> selectedAssetIds = new HashSet<>();
    assetIds.forEach(node -> {
      if (!node.isTextual()) {
        throwInvalidFieldValue();
      }

      String assetId = node.asText();
      try {
        UUID.fromString(assetId);
      } catch (IllegalArgumentException e) {
        throwInvalidFieldValue();
      }

      if (!selectedAssetIds.add(assetId)) {
        throwInvalidFieldValue();
      }
    });
  }

  private boolean hasPresent(JsonNode value) {
    return value != null && !value.isNull();
  }

  private boolean isEmpty(JsonNode value) {
    return value == null || value.isNull() || (value.isArray() && value.isEmpty());
  }

  private void throwInvalidFieldValue() {
    throw new BaseException(OrderFormErrorCode.ORDER_FORM_FIELD_VALUE_INVALID);
  }
}
