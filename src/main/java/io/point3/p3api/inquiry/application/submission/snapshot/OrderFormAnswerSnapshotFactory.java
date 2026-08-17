package io.point3.p3api.inquiry.application.submission.snapshot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.inquiry.application.command.CreateOrderFormSubmissionCommand;
import io.point3.p3api.orderform.application.result.OrderFormFieldResult;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 주문서 답변 저장용 JSON을 만들어준다
 */
@Component
@RequiredArgsConstructor
public class OrderFormAnswerSnapshotFactory {

  private final ObjectMapper objectMapper;

  /**
   * DB에서 조회한 양식 주문서의 필드 정의와 구매자 제출 답변을
   * fieldId 기준으로 매핑해서 저장용 답변 스냅샷 생성
   * @param fields 주문서의 필드
   * @param answers 구매자 제출 답변
   */
  public String create(
      List<OrderFormFieldResult> fields,
      List<CreateOrderFormSubmissionCommand.FormAnswer> answers) {
    Map<UUID, CreateOrderFormSubmissionCommand.FormAnswer> answerMap = answers.stream()
        .collect(Collectors.toMap(
            CreateOrderFormSubmissionCommand.FormAnswer::fieldId, Function.identity()));

    List<AnswerSnapshot> snapshots = fields.stream()
        .sorted(Comparator.comparingInt(OrderFormFieldResult::sortOrder))
        .filter(field -> answerMap.containsKey(field.id()))
        .map(field -> {
          CreateOrderFormSubmissionCommand.FormAnswer answer = answerMap.get(field.id());
          return new AnswerSnapshot(
              field.id(),
              field.label(),
              field.fieldType().name(),
              field.required(),
              field.sortOrder(),
              answer.value());
        })
        .toList();

    try {
      return objectMapper.writeValueAsString(snapshots);
    } catch (JsonProcessingException e) {
      throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  private record AnswerSnapshot(
      UUID fieldId,
      String label,
      String fieldType,
      boolean required,
      int sortOrder,
      Object value) {}
}
