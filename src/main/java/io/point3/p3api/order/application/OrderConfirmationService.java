package io.point3.p3api.order.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.point3.p3api.chat.application.timeline.ChatTimelineItemPublisher;
import io.point3.p3api.chat.domain.entity.ChatTimelineItem;
import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.exception.code.OrderConfirmationErrorCode;
import io.point3.p3api.exception.code.StoreErrorCode;
import io.point3.p3api.inquiry.application.chat.InquiryChatAccessService;
import io.point3.p3api.inquiry.application.port.OrderFormSubmissionPersistencePort;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.inquiry.domain.entity.OrderFormSubmission;
import io.point3.p3api.notification.application.create.CreateNotificationCommand;
import io.point3.p3api.notification.application.create.NotificationCreateUseCase;
import io.point3.p3api.notification.domain.type.NotificationReferenceType;
import io.point3.p3api.notification.domain.type.NotificationType;
import io.point3.p3api.order.application.port.OrderConfirmationPersistencePort;
import io.point3.p3api.order.application.result.SendOrderConfirmationResult;
import io.point3.p3api.order.application.send.SendOrderConfirmationCommand;
import io.point3.p3api.order.application.send.SendOrderConfirmationUseCase;
import io.point3.p3api.order.domain.entity.OrderConfirmation;
import io.point3.p3api.order.domain.type.OrderConfirmationStatus;
import io.point3.p3api.orderform.application.query.OrderFormQueryUseCase;
import io.point3.p3api.orderform.application.result.OrderFormResult;
import io.point3.p3api.store.application.port.StorePersistencePort;
import io.point3.p3api.store.domain.entity.Store;
import java.time.Clock;
import java.time.ZoneId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderConfirmationService implements SendOrderConfirmationUseCase {

  private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

  private final StorePersistencePort storePersistencePort;
  private final InquiryChatAccessService inquiryChatAccessService;
  private final ChatTimelineItemPublisher chatTimelineItemPublisher;
  private final OrderConfirmationPersistencePort orderConfirmationPersistencePort;
  private final OrderFormSubmissionPersistencePort orderFormSubmissionPersistencePort;
  private final OrderFormQueryUseCase orderFormQueryUseCase;
  private final NotificationCreateUseCase notificationCreateUseCase;

  private final Clock clock;
  private final ObjectMapper objectMapper;

  @Override
  public SendOrderConfirmationResult send(SendOrderConfirmationCommand command) {

    // 채팅방이 해당 스토어 소유인지 검증
    Inquiry inquiry =
        inquiryChatAccessService.getSellerInquiry(command.inquiryId(), command.storeId());

    // 최신 제출 주문서가 현재 채팅방 소속인지 검증
    OrderFormSubmission submission = findOrderFormSubmission(command, inquiry);
    OrderFormResult template =
        orderFormQueryUseCase.getSellerTemplate(command.storeId(), submission.getTemplateId());
    ConfirmationAmount amount = calculateAmount(submission, command);
    OrderConfirmation previousConfirmation = orderConfirmationPersistencePort
        .findLatestByInquiryIdAndStatus(inquiry.getId(), OrderConfirmationStatus.SENT)
        .orElse(null);

    // 현재 스토어 조회
    Store store = storePersistencePort
        .findById(command.storeId())
        .orElseThrow(() -> new BaseException(StoreErrorCode.STORE_NOT_FOUND));

    // 주문확정서 생성
    OrderConfirmation confirmation = OrderConfirmation.create(
        inquiry.getId(),
        submission.getId(),
        command.sellerUserId(),
        template.name(),
        command.summaryText(),
        amount.finalAmount(),
        submission
            .getPickupDate()
            .atTime(submission.getPickupTime())
            .atZone(KOREA_ZONE_ID)
            .toInstant(),
        store.getName(),
        createOrderSummary(submission),
        createAdditionalItems(command),
        command.sellerNote());

    // 주문확인서 발송됨 상태로 변경
    confirmation.sent(clock.instant());

    // 저장
    OrderConfirmation savedConfirmation = orderConfirmationPersistencePort.save(confirmation);

    if (previousConfirmation != null) {
      previousConfirmation.replaceWith(savedConfirmation.getId());
    }
    notifyBuyer(inquiry, savedConfirmation, previousConfirmation != null);

    ChatTimelineItem savedTimeLineItem = chatTimelineItemPublisher.publishOrderConfirmation(
        savedConfirmation.getInquiryId(),
        savedConfirmation.getCreatedBy(),
        savedConfirmation.getId());

    return SendOrderConfirmationResult.of(savedConfirmation, savedTimeLineItem);
  }

  private void notifyBuyer(Inquiry inquiry, OrderConfirmation confirmation, boolean isUpdate) {
    notificationCreateUseCase.create(new CreateNotificationCommand(
        inquiry.getBuyerUserId(),
        isUpdate
            ? NotificationType.ORDER_CONFIRMATION_UPDATED
            : NotificationType.ORDER_CONFIRMATION_SENT,
        NotificationReferenceType.ORDER_CONFIRMATION,
        confirmation.getId(),
        isUpdate ? "주문확인서가 변경되었습니다." : "주문확인서가 도착했습니다.",
        "주문확인서를 확인해 주세요."));
  }

  private OrderFormSubmission findOrderFormSubmission(
      SendOrderConfirmationCommand command, Inquiry inquiry) {
    if (command.orderFormSubmissionId() == null) {
      return orderFormSubmissionPersistencePort.findAllByInquiryId(inquiry.getId()).stream()
          .findFirst()
          .orElseThrow(() ->
              new BaseException(OrderConfirmationErrorCode.ORDER_CONFIRMATION_SUBMISSION_INVALID));
    }

    OrderFormSubmission submission = orderFormSubmissionPersistencePort
        .findById(command.orderFormSubmissionId())
        .orElseThrow(() ->
            new BaseException(OrderConfirmationErrorCode.ORDER_CONFIRMATION_SUBMISSION_INVALID));

    if (!submission.getInquiryId().equals(inquiry.getId())) {
      throw new BaseException(OrderConfirmationErrorCode.ORDER_CONFIRMATION_SUBMISSION_INVALID);
    }

    return submission;
  }

  private ConfirmationAmount calculateAmount(
      OrderFormSubmission submission, SendOrderConfirmationCommand command) {
    long additionalAmount = command.additionalItems().stream()
        .map(SendOrderConfirmationCommand.AdditionalItem::amount)
        .filter(java.util.Objects::nonNull)
        .mapToLong(Long::longValue)
        .sum();
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

    long automaticAmount = Math.addExact(baseAmount, additionalAmount);
    if (!inquiryRequired && command.amount() != automaticAmount) {
      throw new BaseException(OrderConfirmationErrorCode.ORDER_CONFIRMATION_AMOUNT_INVALID);
    }
    return new ConfirmationAmount(inquiryRequired ? command.amount() : automaticAmount);
  }

  private String createOrderSummary(OrderFormSubmission submission) {
    if (submission == null) {
      return null;
    }

    OrderSummarySnapshot snapshot = new OrderSummarySnapshot(
        submission.getId(),
        readJson(submission.getAnswers()),
        readNullableJson(submission.getReferenceAssets()));

    return write(snapshot);
  }

  private String createAdditionalItems(SendOrderConfirmationCommand command) {
    if (command.additionalItems() == null || command.additionalItems().isEmpty()) {
      return null;
    }

    return write(command.additionalItems());
  }

  private String write(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  private JsonNode readJson(String value) {
    try {
      return objectMapper.readTree(value);
    } catch (JsonProcessingException e) {
      throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  private JsonNode readNullableJson(String value) {
    if (value == null) {
      return null;
    }

    return readJson(value);
  }

  private record OrderSummarySnapshot(
      UUID orderFormSubmissionId, JsonNode answers, JsonNode referenceAssets) {}

  private record ConfirmationAmount(long finalAmount) {}
}
