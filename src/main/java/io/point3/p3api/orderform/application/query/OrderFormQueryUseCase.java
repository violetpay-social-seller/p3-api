package io.point3.p3api.orderform.application.query;

import io.point3.p3api.orderform.application.result.OrderFormResult;
import java.util.UUID;

public interface OrderFormQueryUseCase {

  OrderFormResult getSellerTemplate(UUID storeId, UUID templateId);

  OrderFormResult getActiveTemplate(UUID storeId);
}
