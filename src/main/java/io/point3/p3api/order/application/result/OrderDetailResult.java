package io.point3.p3api.order.application.result;

import io.point3.p3api.payment.application.result.PaymentAttemptResult;
import io.point3.p3api.payment.application.result.RefundResult;
import java.util.List;

public record OrderDetailResult(
    OrderResult order,
    PaymentAttemptResult paymentAttempt,
    List<RefundResult> refunds,
    List<OrderOptionRow> optionRows) {

  public OrderDetailResult {
    refunds = List.copyOf(refunds);
    optionRows = List.copyOf(optionRows);
  }

  public static OrderDetailResult of(
      OrderResult order,
      PaymentAttemptResult paymentAttempt,
      List<RefundResult> refunds,
      List<OrderOptionRow> optionRows) {
    return new OrderDetailResult(order, paymentAttempt, refunds, optionRows);
  }

  @Override
  public List<RefundResult> refunds() {
    return List.copyOf(refunds);
  }

  @Override
  public List<OrderOptionRow> optionRows() {
    return List.copyOf(optionRows);
  }
}
