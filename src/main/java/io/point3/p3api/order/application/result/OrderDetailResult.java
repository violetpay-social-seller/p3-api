package io.point3.p3api.order.application.result;

import io.point3.p3api.payment.application.result.PaymentAttemptResult;
import io.point3.p3api.payment.application.result.RefundResult;
import java.util.List;

public record OrderDetailResult(
    OrderResult order, PaymentAttemptResult paymentAttempt, List<RefundResult> refunds) {

  public OrderDetailResult {
    refunds = List.copyOf(refunds);
  }

  public static OrderDetailResult of(
      OrderResult order, PaymentAttemptResult paymentAttempt, List<RefundResult> refunds) {
    return new OrderDetailResult(order, paymentAttempt, refunds);
  }

  @Override
  public List<RefundResult> refunds() {
    return List.copyOf(refunds);
  }
}
