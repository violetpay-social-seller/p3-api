package io.point3.p3api.payment.application.query;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderConfirmationErrorCode;
import io.point3.p3api.inquiry.application.chat.InquiryChatAccessService;
import io.point3.p3api.order.application.port.OrderConfirmationPersistencePort;
import io.point3.p3api.order.domain.entity.OrderConfirmation;
import io.point3.p3api.order.domain.type.OrderConfirmationStatus;
import io.point3.p3api.payment.application.port.PaymentAttemptPersistencePort;
import io.point3.p3api.payment.application.result.PaymentAttemptResult;
import io.point3.p3api.payment.application.result.PaymentCtaResult;
import io.point3.p3api.payment.application.result.PaymentCtaStatus;
import io.point3.p3api.payment.domain.entity.PaymentAttempt;
import io.point3.p3api.payment.domain.type.PaymentAttemptStatus;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryService
    implements PaymentCtaQueryUseCase, PaymentAttemptHistoryQueryUseCase {

  private final InquiryChatAccessService inquiryChatAccessService;
  private final OrderConfirmationPersistencePort orderConfirmationPersistencePort;
  private final PaymentAttemptPersistencePort paymentAttemptPersistencePort;
  private final Clock clock;

  @Override
  public PaymentCtaResult getBuyerConfirmationCta(
      UUID inquiryId, UUID confirmationId, UUID buyerUserId) {
    inquiryChatAccessService.getBuyerInquiry(inquiryId, buyerUserId);

    OrderConfirmation confirmation = getConfirmation(inquiryId, confirmationId);
    PaymentAttempt latestPaymentAttempt = getLatestBuyerPaymentAttempt(confirmationId, buyerUserId);

    return buildCtaResult(inquiryId, confirmation, latestPaymentAttempt);
  }

  @Override
  public List<PaymentAttemptResult> getBuyerPaymentAttempts(
      UUID inquiryId, UUID confirmationId, UUID buyerUserId) {
    inquiryChatAccessService.getBuyerInquiry(inquiryId, buyerUserId);

    getConfirmation(inquiryId, confirmationId);

    Instant now = Instant.now(clock);

    return paymentAttemptPersistencePort.findAllByConfirmationId(confirmationId).stream()
        .filter(paymentAttempt -> paymentAttempt.getPayerUserId().equals(buyerUserId))
        .map(paymentAttempt -> PaymentAttemptResult.from(paymentAttempt, now))
        .toList();
  }

  private OrderConfirmation getConfirmation(UUID inquiryId, UUID confirmationId) {
    OrderConfirmation confirmation = orderConfirmationPersistencePort
        .findById(confirmationId)
        .orElseThrow(
            () -> new BaseException(OrderConfirmationErrorCode.ORDER_CONFIRMATION_NOT_FOUND));

    if (!confirmation.getInquiryId().equals(inquiryId)) {
      throw new BaseException(OrderConfirmationErrorCode.ORDER_CONFIRMATION_NOT_FOUND);
    }

    return confirmation;
  }

  private PaymentAttempt getLatestBuyerPaymentAttempt(UUID confirmationId, UUID buyerUserId) {
    return paymentAttemptPersistencePort.findAllByConfirmationId(confirmationId).stream()
        .filter(paymentAttempt -> paymentAttempt.getPayerUserId().equals(buyerUserId))
        .findFirst()
        .orElse(null);
  }

  private PaymentCtaResult buildCtaResult(
      UUID inquiryId, OrderConfirmation confirmation, PaymentAttempt latestPaymentAttempt) {
    Instant now = Instant.now(clock);
    PaymentAttemptResult latestPaymentAttemptResult =
        latestPaymentAttempt == null ? null : PaymentAttemptResult.from(latestPaymentAttempt, now);

    if (confirmation.getStatus() == OrderConfirmationStatus.PAID) {
      return paymentCtaResult(
          inquiryId,
          confirmation,
          false,
          PaymentCtaStatus.PAID,
          "ORDER_CONFIRMATION_ALREADY_PAID",
          latestPaymentAttemptResult);
    }

    if (confirmation.getStatus() != OrderConfirmationStatus.SENT) {
      return paymentCtaResult(
          inquiryId,
          confirmation,
          false,
          PaymentCtaStatus.UNAVAILABLE,
          "ORDER_CONFIRMATION_NOT_SENT",
          latestPaymentAttemptResult);
    }

    if (!isLatestSentConfirmation(confirmation)) {
      return paymentCtaResult(
          inquiryId,
          confirmation,
          false,
          PaymentCtaStatus.UNAVAILABLE,
          "ORDER_CONFIRMATION_NOT_LATEST_SENT",
          latestPaymentAttemptResult);
    }

    if (confirmation.getAmount() <= 0) {
      return paymentCtaResult(
          inquiryId,
          confirmation,
          false,
          PaymentCtaStatus.UNAVAILABLE,
          "ORDER_CONFIRMATION_AMOUNT_INVALID",
          latestPaymentAttemptResult);
    }

    if (confirmation.getBuyerViewedAt() == null) {
      return paymentCtaResult(
          inquiryId,
          confirmation,
          false,
          PaymentCtaStatus.VIEW_REQUIRED,
          "ORDER_CONFIRMATION_NOT_VIEWED",
          latestPaymentAttemptResult);
    }

    return paymentCtaResultForAttempt(
        inquiryId, confirmation, latestPaymentAttempt, latestPaymentAttemptResult, now);
  }

  private boolean isLatestSentConfirmation(OrderConfirmation confirmation) {
    return orderConfirmationPersistencePort
        .findLatestByInquiryIdAndStatus(confirmation.getInquiryId(), OrderConfirmationStatus.SENT)
        .map(latest -> latest.getId().equals(confirmation.getId()))
        .orElse(false);
  }

  private PaymentCtaResult paymentCtaResultForAttempt(
      UUID inquiryId,
      OrderConfirmation confirmation,
      PaymentAttempt latestPaymentAttempt,
      PaymentAttemptResult latestPaymentAttemptResult,
      Instant now) {
    if (latestPaymentAttempt == null) {
      return paymentCtaResult(inquiryId, confirmation, true, PaymentCtaStatus.PAYABLE, null, null);
    }

    if (latestPaymentAttempt.getStatus() == PaymentAttemptStatus.SUCCEEDED) {
      return paymentCtaResult(
          inquiryId,
          confirmation,
          false,
          PaymentCtaStatus.PAID,
          "PAYMENT_ALREADY_SUCCEEDED",
          latestPaymentAttemptResult);
    }

    if (latestPaymentAttempt.getStatus() == PaymentAttemptStatus.NEEDS_CONFIRMATION) {
      return paymentCtaResult(
          inquiryId,
          confirmation,
          false,
          PaymentCtaStatus.PAYMENT_NEEDS_CONFIRMATION,
          "PAYMENT_RESULT_NEEDS_CONFIRMATION",
          latestPaymentAttemptResult);
    }

    if (isActiveAttempt(latestPaymentAttempt, now)) {
      return paymentCtaResult(
          inquiryId,
          confirmation,
          false,
          PaymentCtaStatus.PAYMENT_IN_PROGRESS,
          "PAYMENT_ATTEMPT_IN_PROGRESS",
          latestPaymentAttemptResult);
    }

    if (latestPaymentAttempt.getStatus() == PaymentAttemptStatus.FAILED
        || latestPaymentAttempt.getStatus() == PaymentAttemptStatus.CANCELED
        || now.isAfter(latestPaymentAttempt.getExpiresAt())) {
      return paymentCtaResult(
          inquiryId,
          confirmation,
          true,
          PaymentCtaStatus.RETRY_AVAILABLE,
          null,
          latestPaymentAttemptResult);
    }

    return paymentCtaResult(
        inquiryId, confirmation, true, PaymentCtaStatus.PAYABLE, null, latestPaymentAttemptResult);
  }

  private boolean isActiveAttempt(PaymentAttempt paymentAttempt, Instant now) {
    boolean activeStatus = paymentAttempt.getStatus() == PaymentAttemptStatus.READY
        || paymentAttempt.getStatus() == PaymentAttemptStatus.IN_PROGRESS;

    return activeStatus && !now.isAfter(paymentAttempt.getExpiresAt());
  }

  private PaymentCtaResult paymentCtaResult(
      UUID inquiryId,
      OrderConfirmation confirmation,
      boolean canPay,
      PaymentCtaStatus status,
      String reason,
      PaymentAttemptResult latestPaymentAttempt) {
    return new PaymentCtaResult(
        inquiryId,
        confirmation.getId(),
        confirmation.getAmount(),
        canPay,
        status,
        reason,
        confirmation.getBuyerViewedAt(),
        latestPaymentAttempt);
  }
}
