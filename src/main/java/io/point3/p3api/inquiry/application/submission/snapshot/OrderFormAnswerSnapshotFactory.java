package io.point3.p3api.inquiry.application.submission.snapshot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.inquiry.application.command.CreateOrderFormSubmissionCommand;
import io.point3.p3api.orderform.application.result.OrderFormOptionGroupResult;
import io.point3.p3api.orderform.application.result.OrderFormOptionResult;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 주문서 답변 저장용 JSON을 만들어준다 */
@Component
@RequiredArgsConstructor
public class OrderFormAnswerSnapshotFactory {

  private final ObjectMapper objectMapper;

  /**
   * DB에서 조회한 양식 주문서의 옵션그룹 정의와 구매자 제출 답변을
   * optionGroupId 기준으로 매핑해서 저장용 답변 스냅샷 생성
   */
  public String create(
      List<OrderFormOptionGroupResult> optionGroups,
      List<CreateOrderFormSubmissionCommand.FormAnswer> answers) {
    Map<UUID, CreateOrderFormSubmissionCommand.FormAnswer> answerMap = answers.stream()
        .collect(Collectors.toMap(
            CreateOrderFormSubmissionCommand.FormAnswer::optionGroupId, Function.identity()));

    List<AnswerSnapshot> snapshots = optionGroups.stream()
        .sorted(Comparator.comparingInt(OrderFormOptionGroupResult::sortOrder))
        .filter(optionGroup -> answerMap.containsKey(optionGroup.id()))
        .map(optionGroup -> {
          CreateOrderFormSubmissionCommand.FormAnswer answer = answerMap.get(optionGroup.id());
          return new AnswerSnapshot(
              optionGroup.id(),
              optionGroup.label(),
              optionGroup.selectionType().name(),
              optionGroup.required(),
              optionGroup.sortOrder(),
              answer.value(),
              selectedOptions(optionGroup, answer.value()));
        })
        .toList();

    try {
      return objectMapper.writeValueAsString(snapshots);
    } catch (JsonProcessingException e) {
      throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  private List<OptionSnapshot> selectedOptions(
      OrderFormOptionGroupResult optionGroup, JsonNode value) {
    if (value == null || !value.isArray()) {
      return List.of();
    }

    Map<String, OrderFormOptionResult> optionMap = optionGroup.options().stream()
        .collect(Collectors.toMap(OrderFormOptionResult::value, Function.identity()));

    return value
        .valueStream()
        .filter(JsonNode::isObject)
        .map(selected -> selectedOption(optionMap, selected))
        .filter(java.util.Objects::nonNull)
        .toList();
  }

  private OptionSnapshot selectedOption(
      Map<String, OrderFormOptionResult> optionMap, JsonNode selected) {
    JsonNode optionValueNode = selected.get("optionValue");
    if (optionValueNode == null || !optionValueNode.isTextual()) {
      return null;
    }

    OrderFormOptionResult option = optionMap.get(optionValueNode.asText());
    if (option == null) {
      return null;
    }

    return new OptionSnapshot(
        option.label(),
        option.value(),
        option.inputType().name(),
        option.price(),
        option.settings(),
        text(selected),
        assetIds(selected));
  }

  private String text(JsonNode selected) {
    JsonNode text = selected.get("text");
    return text != null && text.isTextual() ? text.asText() : null;
  }

  private List<String> assetIds(JsonNode selected) {
    JsonNode assetIds = selected.get("assetIds");
    if (assetIds == null || !assetIds.isArray()) {
      return List.of();
    }
    return assetIds
        .valueStream()
        .filter(JsonNode::isTextual)
        .map(JsonNode::asText)
        .toList();
  }

  private record AnswerSnapshot(
      UUID optionGroupId,
      String label,
      String selectionType,
      boolean required,
      int sortOrder,
      Object value,
      List<OptionSnapshot> selectedOptions) {}

  private record OptionSnapshot(
      String label,
      String value,
      String inputType,
      Long price,
      String settings,
      String text,
      List<String> assetIds) {}
}
