package io.point3.p3api.orderform.application.update;

import io.point3.p3api.orderform.application.result.OrderFormResult;
import java.util.UUID;

public interface OrderFormUpdateUseCase {

  OrderFormResult update(UpdateOrderFormCommand command);

  OrderFormResult inactive(UUID storeId, UUID templateId);
}
