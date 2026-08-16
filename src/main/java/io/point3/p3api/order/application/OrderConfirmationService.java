package io.point3.p3api.order.application;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import io.point3.p3api.order.application.port.OrderConfirmationPersistencePort;
import io.point3.p3api.order.application.result.SendOrderConfirmationResult;
import io.point3.p3api.order.application.send.SendOrderConfirmationCommand;
import io.point3.p3api.order.application.send.SendOrderConfirmationUseCase;
import io.point3.p3api.order.domain.entity.OrderConfirmation;
import io.point3.p3api.store.application.port.StorePersistencePort;
import io.point3.p3api.store.domain.entity.Store;
import java.time.Clock;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderConfirmationService implements SendOrderConfirmationUseCase {

  private final StorePersistencePort storePersistencePort;
  private final InquiryChatAccessService inquiryChatAccessService;
  private final ChatTimelineItemPublisher chatTimelineItemPublisher;
  private final OrderConfirmationPersistencePort orderConfirmationPersistencePort;
  private final OrderFormSubmissionPersistencePort orderFormSubmissionPersistencePort;

  private final Clock clock;
  private final ObjectMapper objectMapper;

  @Override
  public SendOrderConfirmationResult sent(SendOrderConfirmationCommand command) {

    // 채팅방이 해당 스토어 소유인지 검증
    Inquiry inquiry =
        inquiryChatAccessService.getSellerInquiry(command.inquiryId(), command.storeId());

    // 주문서가 있다면 현재 채팅방에서 제출된 주문서가 맞는지 검증
    OrderFormSubmission submission = findOrderFormSubmission(command, inquiry);

    // 현재 스토어 조회
    Store store = storePersistencePort
        .findById(command.storeId())
        .orElseThrow(() -> new BaseException(StoreErrorCode.STORE_NOT_FOUND));

    // 주문확정서 생성
    OrderConfirmation confirmation = OrderConfirmation.create(
        inquiry.getId(),
        command.orderFormSubmissionId(),
        command.sellerUserId(),
        command.confirmationTitle(),
        command.summaryText(),
        command.amount(),
        command.pickupAt(),
        store.getName(),
        createOrderSummary(submission),
        createAdditionalItems(command),
        command.sellerNote());

    // 주문확인서 발송됨 상태로 변경
    confirmation.sent(clock.instant());

    // 저장
    OrderConfirmation savedConfirmation = orderConfirmationPersistencePort.save(confirmation);

    ChatTimelineItem savedTimeLineItem = chatTimelineItemPublisher.publishOrderConfirmation(
        savedConfirmation.getInquiryId(),
        savedConfirmation.getCreatedBy(),
        savedConfirmation.getId());

    return SendOrderConfirmationResult.of(savedConfirmation, savedTimeLineItem);
  }

  private OrderFormSubmission findOrderFormSubmission(
      SendOrderConfirmationCommand command, Inquiry inquiry) {
    if (command.orderFormSubmissionId() == null) {
      return null;
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

  private String createOrderSummary(OrderFormSubmission submission) {
    if (submission == null) {
      return null;
    }

    OrderSummarySnapshot snapshot = new OrderSummarySnapshot(
        submission.getId(),
        submission.getProductId(),
        submission.getProductSnapshot(),
        submission.getProductOptionSnapshot(),
        submission.getAnswers());

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

  private record OrderSummarySnapshot(
      UUID orderFormSubmissionId,
      UUID productId,
      String productSnapshot,
      String productOptionSnapshot,
      String answers) {}
}
