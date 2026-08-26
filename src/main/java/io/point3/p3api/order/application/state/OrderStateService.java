package io.point3.p3api.order.application.state;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderErrorCode;
import io.point3.p3api.exception.code.PaymentErrorCode;
import io.point3.p3api.inquiry.application.port.InquiryPersistencePort;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.notification.application.create.CreateNotificationCommand;
import io.point3.p3api.notification.application.create.NotificationCreateUseCase;
import io.point3.p3api.notification.domain.type.NotificationReferenceType;
import io.point3.p3api.notification.domain.type.NotificationType;
import io.point3.p3api.order.application.port.OrderPersistencePort;
import io.point3.p3api.order.application.result.OrderDetailResult;
import io.point3.p3api.order.application.result.OrderResult;
import io.point3.p3api.order.domain.entity.Order;
import io.point3.p3api.payment.application.port.PaymentAttemptPersistencePort;
import io.point3.p3api.payment.application.port.Point3PaymentException;
import io.point3.p3api.payment.application.port.Point3PaymentPort;
import io.point3.p3api.payment.application.port.Point3RefundResult;
import io.point3.p3api.payment.application.port.RefundPersistencePort;
import io.point3.p3api.payment.application.result.PaymentAttemptResult;
import io.point3.p3api.payment.application.result.RefundResult;
import io.point3.p3api.payment.domain.entity.PaymentAttempt;
import io.point3.p3api.payment.domain.entity.Refund;
import io.point3.p3api.payment.domain.type.RefundStatus;
import io.point3.p3api.store.application.port.StorePersistencePort;
import io.point3.p3api.store.domain.entity.Store;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderStateService implements OrderStateUseCase {

  private final OrderPersistencePort orderPersistencePort;
  private final InquiryPersistencePort inquiryPersistencePort;
  private final PaymentAttemptPersistencePort paymentAttemptPersistencePort;
  private final RefundPersistencePort refundPersistencePort;
  private final Point3PaymentPort point3PaymentPort;
  private final Clock clock;
  private final StorePersistencePort storePersistencePort;
  private final NotificationCreateUseCase notificationCreateUseCase;

  @Override
  public OrderResult pickUp(CompleteOrderPickupCommand command) {
    Order order = getSellerOrder(command.orderId(), command.storeId());
    changeStatus(order::markPickedUp);

    Inquiry inquiry = inquiryPersistencePort
        .findById(order.getInquiryId())
        .orElseThrow(() -> new BaseException(OrderErrorCode.ORDER_NOT_FOUND));
    inquiry.markPickedUp();

    return OrderResult.from(order);
  }

  @Override
  public OrderResult requestCancel(RequestOrderCancelCommand command) {
    Order order = orderPersistencePort
        .findByIdAndBuyerUserId(command.orderId(), command.buyerUserId())
        .orElseThrow(() -> new BaseException(OrderErrorCode.ORDER_NOT_FOUND));

    changeStatus(() -> order.requestCancel(command.reason(), Instant.now(clock)));
    notifySellerCancelRequested(order);

    return OrderResult.from(order);
  }

  @Override
  public OrderDetailResult refund(RefundOrderCommand command) {
    Order order = getSellerOrder(command.orderId(), command.storeId());
    Refund refund = Refund.create(
        order.getId(),
        order.getPaymentAttemptId(),
        command.sellerUserId(),
        order.getPaidAmount(),
        command.reason());
    refund = refundPersistencePort.save(refund);
    PaymentAttempt paymentAttempt = paymentAttemptPersistencePort
        .findById(order.getPaymentAttemptId())
        .orElseThrow(() -> new BaseException(PaymentErrorCode.PAYMENT_ATTEMPT_NOT_FOUND));
    try {
      Point3RefundResult result = point3PaymentPort.refund(
          paymentAttempt.getPoint3SessionId(),
          order.getPaidAmount(),
          command.reason(),
          refund.getId().toString());
      if (result.completed()) {
        changeStatus(() -> order.refund(command.reason()));
        refund.complete(Instant.now(clock));
      } else {
        refund.fail();
      }
    } catch (Point3PaymentException exception) {
      refund.fail();
    }
    refundPersistencePort.save(refund);
    notifyBuyerRefundResult(order, refund.getStatus());

    return toDetail(order);
  }

  private Order getSellerOrder(UUID orderId, UUID storeId) {
    return orderPersistencePort
        .findByIdAndStoreId(orderId, storeId)
        .orElseThrow(() -> new BaseException(OrderErrorCode.ORDER_NOT_FOUND));
  }

  private void changeStatus(Runnable transition) {
    try {
      transition.run();
    } catch (IllegalStateException e) {
      throw new BaseException(OrderErrorCode.ORDER_STATUS_FORBIDDEN);
    }
  }

  private void notifySellerCancelRequested(Order order) {
    Store store = storePersistencePort
        .findById(order.getStoreId())
        .orElseThrow(() -> new BaseException(OrderErrorCode.ORDER_NOT_FOUND));
    notificationCreateUseCase.create(new CreateNotificationCommand(
        store.getOwnerUserId(),
        NotificationType.ORDER_CANCEL_REQUESTED,
        NotificationReferenceType.ORDER,
        order.getId(),
        "주문 취소가 요청되었습니다.",
        "취소 요청 주문을 확인해 주세요."));
  }

  private void notifyBuyerRefundResult(Order order, RefundStatus status) {
    notificationCreateUseCase.create(new CreateNotificationCommand(
        order.getBuyerUserId(),
        status == RefundStatus.COMPLETED
            ? NotificationType.ORDER_REFUNDED
            : NotificationType.ORDER_REFUND_FAILED,
        NotificationReferenceType.ORDER,
        order.getId(),
        status == RefundStatus.COMPLETED ? "주문 환불이 완료되었습니다." : "주문 환불에 실패했습니다.",
        "환불 내역을 확인해 주세요."));
  }

  private OrderDetailResult toDetail(Order order) {
    Instant now = Instant.now(clock);
    PaymentAttempt paymentAttempt = paymentAttemptPersistencePort
        .findById(order.getPaymentAttemptId())
        .orElseThrow(() -> new BaseException(PaymentErrorCode.PAYMENT_ATTEMPT_NOT_FOUND));
    List<RefundResult> refunds = refundPersistencePort.findAllByOrderId(order.getId()).stream()
        .map(RefundResult::from)
        .toList();

    return OrderDetailResult.of(
        OrderResult.from(order), PaymentAttemptResult.from(paymentAttempt, now), refunds);
  }
}
