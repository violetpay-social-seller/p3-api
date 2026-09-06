package io.point3.p3api.order.controller.response;

import io.point3.p3api.order.application.result.OrderDetailResult;
import io.point3.p3api.payment.controller.response.PaymentAttemptResponse;
import io.point3.p3api.payment.controller.response.RefundResponse;
import java.util.List;

public record OrderDetailResponse(
    OrderResponse order,
    PaymentAttemptResponse paymentAttempt,
    List<RefundResponse> refunds,
    List<OrderOptionRowResponse> optionRows) {

  public OrderDetailResponse {
    refunds = List.copyOf(refunds);
    optionRows = List.copyOf(optionRows);
  }

  public static OrderDetailResponse from(OrderDetailResult result) {
    return new OrderDetailResponse(
        OrderResponse.from(result.order()),
        PaymentAttemptResponse.from(result.paymentAttempt()),
        result.refunds().stream().map(RefundResponse::from).toList(),
        result.optionRows().stream().map(OrderOptionRowResponse::from).toList());
  }

  @Override
  public List<RefundResponse> refunds() {
    return List.copyOf(refunds);
  }

  @Override
  public List<OrderOptionRowResponse> optionRows() {
    return List.copyOf(optionRows);
  }
}
