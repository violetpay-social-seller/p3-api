package io.point3.p3api.order.application.option;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.order.application.result.OrderOptionRow;
import io.point3.p3api.order.domain.entity.OrderConfirmation;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderOptionRowResolver {

  private final ObjectMapper objectMapper;

  public List<OrderOptionRow> fromConfirmation(OrderConfirmation confirmation) {
    return fromConfirmation(confirmation.getOrderSummary(), confirmation.getAdditionalItems());
  }

  public List<OrderOptionRow> fromConfirmation(String orderSummary, String additionalItems) {
    List<OrderOptionRow> rows = new ArrayList<>();
    rows.addAll(fromOrderSummary(orderSummary));
    rows.addAll(fromAdditionalItems(additionalItems));
    return List.copyOf(rows);
  }

  public List<OrderOptionRow> fromSubmissionAnswers(String answers) {
    if (isBlank(answers)) {
      return List.of();
    }
    return fromAnswers(read(answers));
  }

  private List<OrderOptionRow> fromOrderSummary(String orderSummary) {
    if (isBlank(orderSummary)) {
      return List.of();
    }
    JsonNode answers = read(orderSummary).path("answers");
    return fromAnswers(answers);
  }

  private List<OrderOptionRow> fromAnswers(JsonNode answers) {
    if (!answers.isArray()) {
      return List.of();
    }

    List<OrderOptionRow> rows = new ArrayList<>();
    for (JsonNode answer : answers) {
      String label = text(answer.path("label"));
      JsonNode selectedOptions = answer.path("selectedOptions");
      if (selectedOptions.isArray() && !selectedOptions.isEmpty()) {
        selectedOptions.forEach(option -> addSelectedOption(rows, label, option));
      } else {
        addAnswerValue(rows, label, answer);
      }
    }
    return List.copyOf(rows);
  }

  private void addSelectedOption(List<OrderOptionRow> rows, String label, JsonNode option) {
    String value = firstText(option.path("text"), option.path("label"), option.path("value"));
    addRow(rows, label, value, amount(option.path("price")));
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
}
