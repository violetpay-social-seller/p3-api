package io.point3.p3api.order.application.query.order;

import io.point3.p3api.exception.BaseException;
import io.point3.p3api.exception.code.OrderErrorCode;
import io.point3.p3api.exception.code.PaymentErrorCode;
import io.point3.p3api.order.application.port.OrderPersistencePort;
import io.point3.p3api.order.application.result.OrderDetailResult;
import io.point3.p3api.order.application.result.OrderResult;
import io.point3.p3api.order.domain.entity.Order;
import io.point3.p3api.payment.application.port.PaymentAttemptPersistencePort;
import io.point3.p3api.payment.application.port.RefundPersistencePort;
import io.point3.p3api.payment.application.result.PaymentAttemptResult;
import io.point3.p3api.payment.application.result.RefundResult;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderQueryService implements OrderQueryUseCase {
  private final OrderPersistencePort orderPersistencePort;
  private final PaymentAttemptPersistencePort paymentAttemptPersistencePort;
  private final RefundPersistencePort refundPersistencePort;
  private final Clock clock;

  @Override
  public List<OrderResult> getBuyerOrders(UUID buyerUserId) {
    return orderPersistencePort.findAllByBuyerUserId(buyerUserId).stream()
        .map(OrderResult::from)
        .toList();
  }

  @Override
  public OrderDetailResult getBuyerOrder(UUID orderId, UUID buyerUserId) {
    return toDetail(orderPersistencePort
        .findByIdAndBuyerUserId(orderId, buyerUserId)
        .orElseThrow(() -> new BaseException(OrderErrorCode.ORDER_NOT_FOUND)));
  }

  @Override
  public List<OrderResult> getSellerOrders(UUID storeId) {
    return orderPersistencePort.findAllByStoreId(storeId).stream()
        .map(OrderResult::from)
        .toList();
  }

  @Override
  public OrderDetailResult getSellerOrder(UUID orderId, UUID storeId) {
    return toDetail(orderPersistencePort
        .findByIdAndStoreId(orderId, storeId)
        .orElseThrow(() -> new BaseException(OrderErrorCode.ORDER_NOT_FOUND)));
  }

  private OrderDetailResult toDetail(Order order) {
    Instant now = Instant.now(clock);

    PaymentAttemptResult paymentAttempt = paymentAttemptPersistencePort
        .findById(order.getPaymentAttemptId())
        .map(attempt -> PaymentAttemptResult.from(attempt, now))
        .orElseThrow(() -> new BaseException(PaymentErrorCode.PAYMENT_ATTEMPT_NOT_FOUND));

    List<RefundResult> refunds = refundPersistencePort.findAllByOrderId(order.getId()).stream()
        .map(RefundResult::from)
        .toList();

    return OrderDetailResult.of(OrderResult.from(order), paymentAttempt, refunds);
  }
}
