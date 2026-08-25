package io.point3.p3api.order.application.state;

import io.point3.p3api.order.application.result.OrderDetailResult;
import io.point3.p3api.order.application.result.OrderResult;

public interface OrderStateUseCase {

  OrderResult pickUp(CompleteOrderPickupCommand command);

  OrderResult requestCancel(RequestOrderCancelCommand command);

  OrderDetailResult refund(RefundOrderCommand command);
}
