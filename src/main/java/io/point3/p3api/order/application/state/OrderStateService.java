package io.point3.p3api.order.application.state;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderErrorCode;
import io.point3.p3api.exception.code.PaymentErrorCode;
import io.point3.p3api.inquiry.application.port.InquiryPersistencePort;
import io.point3.p3api.inquiry.domain.entity.Inquiry;
import io.point3.p3api.order.application.port.OrderPersistencePort;
import io.point3.p3api.order.application.result.OrderDetailResult;
import io.point3.p3api.order.application.result.OrderResult;
import io.point3.p3api.order.domain.entity.Order;
import io.point3.p3api.payment.application.port.PaymentAttemptPersistencePort;
import io.point3.p3api.payment.application.port.RefundPersistencePort;
import io.point3.p3api.payment.application.result.PaymentAttemptResult;
import io.point3.p3api.payment.application.result.RefundResult;
import io.point3.p3api.payment.domain.entity.PaymentAttempt;
import io.point3.p3api.payment.domain.entity.Refund;
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
  private final Clock clock;

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

    return OrderResult.from(order);
  }

  @Override
  public OrderDetailResult refund(RefundOrderCommand command) {
    Order order = getSellerOrder(command.orderId(), command.storeId());
    changeStatus(() -> order.refund(command.reason()));

    Refund refund = Refund.create(
        order.getId(),
        order.getPaymentAttemptId(),
        command.sellerUserId(),
        order.getPaidAmount(),
        command.reason());
    refund.complete(Instant.now(clock));
    refundPersistencePort.save(refund);

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
