package io.point3.p3api.order.application.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.exception.code.OrderConfirmationErrorCode;
import io.point3.p3api.inquiry.application.chat.InquiryChatAccessService;
import io.point3.p3api.inquiry.application.port.OrderFormSubmissionPersistencePort;
import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import io.point3.p3api.orderform.application.query.OrderFormQueryUseCase;
import java.time.ZoneId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 최신 제출 주문서 기반 주문확인서 미리보기 조회 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderConfirmationPreviewQueryService {

  private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

  private final InquiryChatAccessService inquiryChatAccessService;
  private final OrderFormSubmissionPersistencePort submissionPersistencePort;
  private final OrderFormQueryUseCase orderFormQueryUseCase;
  private final ObjectMapper objectMapper;

  public OrderConfirmationPreview getPreview(UUID inquiryId, UUID storeId) {
    inquiryChatAccessService.getSellerInquiry(inquiryId, storeId);
    OrderFormSubmission submission =
        submissionPersistencePort.findAllByInquiryId(inquiryId).stream()
            .findFirst()
            .orElseThrow(() -> new BaseException(
                OrderConfirmationErrorCode.ORDER_CONFIRMATION_SUBMISSION_INVALID));
    long baseAmount = 0;
    boolean inquiryRequired = false;
    try {
      for (JsonNode answer : objectMapper.readTree(submission.getAnswers())) {
        for (JsonNode option : answer.path("selectedOptions")) {
          try {
            baseAmount =
                Math.addExact(baseAmount, Long.parseLong(option.path("value").asText()));
          } catch (NumberFormatException e) {
            inquiryRequired = true;
          }
        }
      }
    } catch (JsonProcessingException e) {
      throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
    return new OrderConfirmationPreview(
        submission.getId(),
        orderFormQueryUseCase
            .getSellerTemplate(storeId, submission.getTemplateId())
            .name(),
        submission
            .getPickupDate()
            .atTime(submission.getPickupTime())
            .atZone(KOREA_ZONE_ID)
            .toInstant(),
        submission.getAnswers(),
        baseAmount,
        inquiryRequired);
  }
}
