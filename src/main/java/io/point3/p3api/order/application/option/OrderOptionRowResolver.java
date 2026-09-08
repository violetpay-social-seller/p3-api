package io.point3.p3api.order.application.option;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.order.application.result.OrderOptionRow;
import io.point3.p3api.order.domain.entity.OrderConfirmation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderOptionRowResolver {

  private final ObjectMapper objectMapper;

  public List<OrderOptionRow> fromConfirmation(OrderConfirmation confirmation) {
    return fromConfirmation(
        confirmation.getOrderSummary(),
        confirmation.getConfirmedOptionPrices(),
        confirmation.getAdditionalItems());
  }

  public List<OrderOptionRow> fromConfirmation(String orderSummary, String additionalItems) {
    return fromConfirmation(orderSummary, null, additionalItems);
  }

  public List<OrderOptionRow> fromConfirmation(
      String orderSummary, String confirmedOptionPrices, String additionalItems) {
    List<OrderOptionRow> rows = new ArrayList<>();
    rows.addAll(fromOrderSummary(orderSummary, confirmedOptionPrices));
    rows.addAll(fromAdditionalItems(additionalItems));
    return List.copyOf(rows);
  }

  public List<OrderOptionRow> fromSubmissionAnswers(String answers) {
    if (isBlank(answers)) {
      return List.of();
    }
    return fromAnswers(read(answers), Map.of());
  }

  private List<OrderOptionRow> fromOrderSummary(String orderSummary, String confirmedOptionPrices) {
    if (isBlank(orderSummary)) {
      return List.of();
    }
    JsonNode answers = read(orderSummary).path("answers");
    return fromAnswers(answers, confirmedAmounts(confirmedOptionPrices));
  }

  private List<OrderOptionRow> fromAnswers(
      JsonNode answers, Map<OptionKey, Long> confirmedAmounts) {
    if (!answers.isArray()) {
      return List.of();
    }

    List<OrderOptionRow> rows = new ArrayList<>();
    for (JsonNode answer : answers) {
      String label = text(answer.path("label"));
      JsonNode selectedOptions = answer.path("selectedOptions");
      if (selectedOptions.isArray() && !selectedOptions.isEmpty()) {
        String optionGroupId = text(answer.path("optionGroupId"));
        selectedOptions.forEach(
            option -> addSelectedOption(rows, label, optionGroupId, option, confirmedAmounts));
      } else {
        addAnswerValue(rows, label, answer);
      }
    }
    return List.copyOf(rows);
  }

  private void addSelectedOption(
      List<OrderOptionRow> rows,
      String label,
      String optionGroupId,
      JsonNode option,
      Map<OptionKey, Long> confirmedAmounts) {
    String value = firstText(option.path("text"), option.path("label"), option.path("value"));
    Long confirmedAmount =
        confirmedAmounts.get(new OptionKey(optionGroupId, text(option.path("value"))));
    addRow(
        rows,
        label,
        value,
        confirmedAmount == null ? amount(option.path("price")) : confirmedAmount);
  }

  private void addAnswerValue(List<OrderOptionRow> rows, String label, JsonNode answer) {
    String value = text(answer.path("value"));
    addRow(rows, label, value, amount(answer.path("price")));
  }

  private List<OrderOptionRow> fromAdditionalItems(String additionalItems) {
    if (isBlank(additionalItems)) {
      return List.of();
    }

    JsonNode root = read(additionalItems);
    if (!root.isArray()) {
      return List.of();
    }

    List<OrderOptionRow> rows = new ArrayList<>();
    root.forEach(item -> addRow(
        rows, text(item.path("label")), text(item.path("value")), amount(item.path("amount"))));
    return List.copyOf(rows);
  }

  private Map<OptionKey, Long> confirmedAmounts(String confirmedOptionPrices) {
    if (isBlank(confirmedOptionPrices)) {
      return Map.of();
    }
    JsonNode root = read(confirmedOptionPrices);
    if (!root.isArray()) {
      throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
    Map<OptionKey, Long> amounts = new HashMap<>();
    for (JsonNode item : root) {
      String optionGroupId = text(item.path("optionGroupId"));
      String optionValue = text(item.path("optionValue"));
      Long amount = amount(item.path("amount"));
      if (isBlank(optionGroupId) || isBlank(optionValue) || amount == null) {
        throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
      }
      amounts.put(new OptionKey(optionGroupId, optionValue), amount);
    }
    return Map.copyOf(amounts);
  }

  private void addRow(List<OrderOptionRow> rows, String label, String value, Long amount) {
    if (isBlank(label) || isBlank(value)) {
      return;
    }
    rows.add(new OrderOptionRow(label, value, amount));
  }

  private Long amount(JsonNode node) {
    if (node.isMissingNode() || node.isNull()) {
      return null;
    }
    if (!node.canConvertToLong()) {
      throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
    return node.asLong();
  }

  private String firstText(JsonNode... nodes) {
    for (JsonNode node : nodes) {
      String value = text(node);
      if (!isBlank(value)) {
        return value;
      }
    }
    return null;
  }

  private String text(JsonNode node) {
    if (node == null || node.isMissingNode() || node.isNull()) {
      return null;
    }
    if (node.isTextual()) {
      return node.asText();
    }
    if (node.isNumber() || node.isBoolean()) {
      return node.asText();
    }
    return null;
  }

  private JsonNode read(String value) {
    try {
      return objectMapper.readTree(value);
    } catch (JsonProcessingException e) {
      throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private record OptionKey(String optionGroupId, String optionValue) {}
}
