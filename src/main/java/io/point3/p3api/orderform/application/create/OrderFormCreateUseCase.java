package io.point3.p3api.orderform.application.create;

import io.point3.p3api.orderform.application.result.OrderFormResult;

public interface OrderFormCreateUseCase {

  OrderFormResult create(CreateOrderFormCommand command);
}
