package io.point3.p3api.payment.application;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.CommonErrorCode;
import io.point3.p3api.exception.code.OrderConfirmationErrorCode;
import io.point3.p3api.exception.code.PaymentErrorCode;
import io.point3.p3api.inquiry.application.chat.InquiryChatAccessService;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.order.application.port.OrderConfirmationPersistencePort;
import io.point3.p3api.order.application.port.OrderPersistencePort;
import io.point3.p3api.order.domain.entity.Order;
import io.point3.p3api.order.domain.entity.OrderConfirmation;
import io.point3.p3api.order.domain.type.OrderConfirmationStatus;
import io.point3.p3api.payment.application.capture.CapturePaymentCommand;
import io.point3.p3api.payment.application.capture.PaymentCaptureUseCase;
import io.point3.p3api.payment.application.port.PaymentAttemptPersistencePort;
import io.point3.p3api.payment.application.port.Point3PaymentException;
import io.point3.p3api.payment.application.port.Point3PaymentPort;
import io.point3.p3api.payment.application.prepare.PaymentPrepareUseCase;
import io.point3.p3api.payment.application.prepare.PreparePaymentCommand;
import io.point3.p3api.payment.application.result.PaymentCaptureResult;
import io.point3.p3api.payment.application.result.PaymentPreparationResult;
import io.point3.p3api.payment.application.result.Point3CaptureResult;
import io.point3.p3api.payment.application.result.Point3PaymentSession;
import io.point3.p3api.payment.config.Point3Properties;
import io.point3.p3api.payment.domain.entity.PaymentAttempt;
import io.point3.p3api.user.application.port.UserPersistencePort;
import io.point3.p3api.user.domain.entity.User;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService implements PaymentPrepareUseCase, PaymentCaptureUseCase {

  private static final DateTimeFormatter ORDER_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

  private final InquiryChatAccessService inquiryChatAccessService;
  private final OrderConfirmationPersistencePort orderConfirmationPersistencePort;
  private final PaymentAttemptPersistencePort paymentAttemptPersistencePort;
  private final OrderPersistencePort orderPersistencePort;
  private final UserPersistencePort userPersistencePort;
  private final Point3PaymentPort point3PaymentPort;
  private final Point3Properties point3Properties;
  private final Clock clock;

  @Override
  public PaymentPreparationResult prepare(PreparePaymentCommand command) {
    inquiryChatAccessService.getBuyerInquiry(command.inquiryId(), command.buyerUserId());

    OrderConfirmation confirmation = getPayableConfirmation(command);

    User payer = userPersistencePort
        .findById(command.buyerUserId())
        .orElseThrow(() -> new BaseException(CommonErrorCode.UNAUTHORIZED));

    Point3PaymentSession session = createPoint3Session(confirmation);

    if (session.amount() != confirmation.getAmount()) {
      throw new BaseException(PaymentErrorCode.PAYMENT_EXTERNAL_UNAVAILABLE);
    }

    Instant expiresAt = Instant.now(clock).plus(point3Properties.sessionTtl());

    PaymentAttempt paymentAttempt = paymentAttemptPersistencePort.save(PaymentAttempt.create(
        confirmation.getId(),
        command.buyerUserId(),
        session.sessionId(),
        payer.getPayerId(),
        confirmation.getAmount(),
        expiresAt));

    return buildPreparation(paymentAttempt, payer.getPayerId());
  }

  @Override
  public PaymentCaptureResult capture(CapturePaymentCommand command) {
    validateCaptureMessage(command);

    PaymentAttempt paymentAttempt = getPaymentAttempt(command.paymentAttemptId());

    validateCaptureOwner(paymentAttempt, command.buyerUserId());
    validateSession(paymentAttempt, command.sessionId());

    Optional<Order> existingOrder =
        orderPersistencePort.findByPaymentAttemptId(paymentAttempt.getId());

    if (!paymentAttempt.isReady()) {
      return PaymentCaptureResult.of(
          paymentAttempt, existingOrder.map(Order::getId).orElse(null));
    }

    if (Instant.now(clock).isAfter(paymentAttempt.getExpiresAt())) {
      paymentAttempt.fail("PAYMENT_SESSION_EXPIRED", Instant.now(clock));
      return PaymentCaptureResult.of(paymentAttempt, null);
    }

    paymentAttempt.startCapture();
    Point3CaptureResult captureResult = requestCapture(paymentAttempt.getPoint3SessionId());
    validatePoint3Session(paymentAttempt, captureResult);

    if (captureResult.status() == Point3CaptureResult.Status.CAPTURED) {
      return completePayment(paymentAttempt, command.payerId());
    }

    Instant completedAt = Instant.now(clock);

    if (captureResult.status() == Point3CaptureResult.Status.FAILED) {
      paymentAttempt.fail(captureResult.failureCode(), completedAt);
    } else {
      paymentAttempt.needConfirmation(captureResult.failureCode(), completedAt);
    }

    return PaymentCaptureResult.of(paymentAttempt, null);
  }

  private OrderConfirmation getPayableConfirmation(PreparePaymentCommand command) {
    OrderConfirmation confirmation = getConfirmation(command.inquiryId(), command.confirmationId());

    Optional<OrderConfirmation> latestSentConfirmation =
        orderConfirmationPersistencePort.findLatestByInquiryIdAndStatus(
            command.inquiryId(), OrderConfirmationStatus.SENT);

    boolean payable = latestSentConfirmation
        .map(latest -> latest.getId().equals(confirmation.getId()))
        .orElse(false);

    if (!payable || confirmation.getBuyerViewedAt() == null || confirmation.getAmount() <= 0) {
      throw new BaseException(PaymentErrorCode.PAYMENT_CONFIRMATION_NOT_PAYABLE);
    }

    return confirmation;
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

  private Point3PaymentSession createPoint3Session(OrderConfirmation confirmation) {
    try {
      return point3PaymentPort.createSession(
          confirmation.getAmount(),
          confirmation.getMenuName(),
          confirmation.getStoreNameSnapshot());
    } catch (Point3PaymentException e) {
      throw new BaseException(PaymentErrorCode.PAYMENT_EXTERNAL_UNAVAILABLE);
    }
  }

  private PaymentPreparationResult buildPreparation(PaymentAttempt paymentAttempt, String payerId) {
    String authnState = paymentAttempt.getId().toString();
    String entryPath = payerId == null ? "/regist" : "/login";
    String authenticationUrl =
        buildAuthenticationUrl(entryPath, paymentAttempt.getPoint3SessionId(), payerId, authnState);

    return new PaymentPreparationResult(
        paymentAttempt.getId(),
        paymentAttempt.getPoint3SessionId(),
        paymentAttempt.getAmount(),
        payerId,
        point3Properties.clientId(),
        authnState,
        entryPath,
        authenticationUrl,
        point3Properties.paymentOrigin(),
        paymentAttempt.getExpiresAt());
  }

  private String buildAuthenticationUrl(
      String entryPath, String sessionId, String payerId, String authnState) {
    StringBuilder url = new StringBuilder(point3Properties.authBaseUrl())
        .append(entryPath)
        .append("?client_id=")
        .append(encode(point3Properties.clientId()))
        .append("&session_id=")
        .append(encode(sessionId))
        .append("&state=")
        .append(encode(authnState));

    if (payerId != null) {
      url.append("&payer_id=").append(encode(payerId));
    }

    return url.toString();
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  private void validateCaptureMessage(CapturePaymentCommand command) {
    if (isBlank(command.sessionId()) || isBlank(command.payerId())) {
      throw new BaseException(PaymentErrorCode.PAYMENT_CAPTURE_MESSAGE_INVALID);
    }
  }

  private PaymentAttempt getPaymentAttempt(UUID paymentAttemptId) {
    return paymentAttemptPersistencePort
        .findById(paymentAttemptId)
        .orElseThrow(() -> new BaseException(PaymentErrorCode.PAYMENT_ATTEMPT_NOT_FOUND));
  }

  private void validateCaptureOwner(PaymentAttempt paymentAttempt, UUID buyerUserId) {
    if (!paymentAttempt.getPayerUserId().equals(buyerUserId)) {
      throw new BaseException(PaymentErrorCode.PAYMENT_ATTEMPT_FORBIDDEN);
    }
  }

  private void validateSession(PaymentAttempt paymentAttempt, String sessionId) {
    if (!paymentAttempt.getPoint3SessionId().equals(sessionId)) {
      throw new BaseException(PaymentErrorCode.PAYMENT_SESSION_MISMATCH);
    }
  }

  private Point3CaptureResult requestCapture(String sessionId) {
    try {
      return point3PaymentPort.capture(sessionId);
    } catch (Point3PaymentException e) {
      return new Point3CaptureResult(
          sessionId, Point3CaptureResult.Status.PROCESSING, e.getFailureCode());
    }
  }

  private void validatePoint3Session(
      PaymentAttempt paymentAttempt, Point3CaptureResult captureResult) {
    if (!paymentAttempt.getPoint3SessionId().equals(captureResult.sessionId())) {
      throw new BaseException(PaymentErrorCode.PAYMENT_SESSION_MISMATCH);
    }
  }

  private PaymentCaptureResult completePayment(PaymentAttempt paymentAttempt, String payerId) {
    Instant completedAt = Instant.now(clock);

    OrderConfirmation confirmation = orderConfirmationPersistencePort
        .findById(paymentAttempt.getConfirmationId())
        .orElseThrow(
            () -> new BaseException(OrderConfirmationErrorCode.ORDER_CONFIRMATION_NOT_FOUND));

    Inquiry inquiry = inquiryChatAccessService.getBuyerInquiry(
        confirmation.getInquiryId(), paymentAttempt.getPayerUserId());

    User payer = userPersistencePort
        .findById(paymentAttempt.getPayerUserId())
        .orElseThrow(() -> new BaseException(CommonErrorCode.UNAUTHORIZED));

    paymentAttempt.succeed(payerId, completedAt);
    payer.connectPayer(payerId);
    confirmation.markPaid();
    inquiry.markPaid();

    Order order = orderPersistencePort
        .findByPaymentAttemptId(paymentAttempt.getId())
        .orElseGet(() -> orderPersistencePort.save(Order.create(
            inquiry.getStoreId(),
            paymentAttempt.getPayerUserId(),
            confirmation.getInquiryId(),
            confirmation.getId(),
            paymentAttempt.getId(),
            createOrderNumber(paymentAttempt),
            confirmation.getMenuName(),
            confirmation.getOptionSummary(),
            paymentAttempt.getAmount(),
            confirmation.getPickupAt())));

    return PaymentCaptureResult.of(paymentAttempt, order.getId());
  }

  private String createOrderNumber(PaymentAttempt paymentAttempt) {
    String date =
        ORDER_DATE_FORMATTER.format(Instant.now(clock).atZone(clock.getZone()).toLocalDate());
    String suffix = paymentAttempt.getId().toString().replace("-", "").substring(0, 27);
    return "P3-" + date + "-" + suffix;
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
